package cn.zhaiyifan.lyric.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

import cn.zhaiyifan.lyric.LyricUtils;
import cn.zhaiyifan.lyric.model.Lyric;

/**
 * A Scrollable TextView which use lyric stream as input and display it.
 * <p/>
 * Created by yifan on 5/13/14.
 */
public class LyricView extends TextView implements Runnable {
    private static final String TAG = "LyricView";
    public static final boolean DEBUG = true;

    /**
     * 当前行在顶部显示;
     */
    public static final int TOP = 0;

    /**
     * 当前行在中间显示;
     */
    public static final int Center = 1;

    private Lyric lyric;

    private static final int DY = 50;

    /**
     * 当前歌词画笔.
     */
    private Paint mCurrentPaint;
    private Paint mNextPaint;
    private Paint mPaint;

    private float mMiddleX;
    private float mMiddleY;

    private int mHeight;

    /**
     * 背景色.
     */
    private int mBackgroundColor = Color.BLUE;
//    private int mBackgroundColor = Color.TRANSPARENT;

    /**
     * 当前歌词颜色.
     */
    private int mHighlightColor = Color.WHITE;

    /**
     * 当前歌词颜色.
     */
    private int mNextLineColor = Color.argb(153, 255, 255, 255);

    /**
     * 默认歌词颜色.
     */
    private int mNormalColor = Color.argb(96, 255, 255, 255);

    /**
     * 字体大小, px.
     * 默认 14dp.
     */
    private int mTextSize = 14;

    /**
     * 歌词索引.
     */
    private int mLyricIndex = 0;

    /**
     * 歌词行数.
     */
    private int mLyricSentenceLength;

    /**
     * 是否需要更新.
     */
    private boolean mNeedUpdate = false;

    /**
     * 最后动作Y轴位置.
     */
    private float mLastEffectY = 0;

    private int mIsTouched = 0;

    /**
     * 开始的时间.
     */
    private long mStartTime = -1;

    /**
     * 是否需要停止.
     */
    private boolean mNeedStop = true;

    /**
     * 是否在前台.
     */
    private boolean mIsForeground = true;

    /**
     * 下一句歌词应该展示的时间.
     */
    private long mNextSentenceTime = -1;

    /**
     * 是否正在展示.
     */
    private boolean mPlaying = false;

    /**
     * 是否需要拖动歌词.
     */
    private boolean mCanDrag = false;

    private int mCurrGravity = TOP;

    private OnLyricUpdateListener mOnLyricUpdateListener;

    public LyricView(Context context) {
        this(context, null);
    }

    public LyricView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LyricView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setFocusable(true);

        setBackgroundColor(mBackgroundColor);

        mTextSize = dp2px(mTextSize);

        // Non-highlight part
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(mTextSize);
        mPaint.setColor(mNormalColor);
        mPaint.setTypeface(Typeface.SERIF);

        // highlight part, current lyric
        mCurrentPaint = new Paint();
        mCurrentPaint.setAntiAlias(true);
        mCurrentPaint.setColor(mHighlightColor);
        mCurrentPaint.setTextSize(mTextSize);
        mCurrentPaint.setTypeface(Typeface.SANS_SERIF);

        // next lyric.
        mNextPaint = new Paint();
        mNextPaint.setAntiAlias(true);
        mNextPaint.setColor(mNextLineColor);
        mNextPaint.setTextSize(mTextSize);
        mNextPaint.setTypeface(Typeface.SANS_SERIF);


        mPaint.setTextAlign(Paint.Align.CENTER);
        mNextPaint.setTextAlign(Paint.Align.CENTER);
        mCurrentPaint.setTextAlign(Paint.Align.CENTER);

        setHorizontallyScrolling(true);
        setMovementMethod(new ScrollingMovementMethod());
    }

    public void setOnLyricUpdateListener(OnLyricUpdateListener lister) {
        mOnLyricUpdateListener = lister;
    }

    private int drawText(Canvas canvas, Paint paint, String text, float startY) {
        if (DEBUG) {
            Log.d(TAG, "drawText() called with: text = [" + text + "], startY = [" + startY + "]");
        }
        int line = 0;
        float textWidth = mPaint.measureText(text);
        final int width = getWidth() - 85;
        if (textWidth > width) {
            int length = text.length();
            int startIndex = 0;
            int endIndex = Math.min((int) ((float) length * (width / textWidth)), length - 1);
            // 每行文字最多的个数.
            int perLineLength = endIndex - startIndex;

            LinkedList<String> lines = new LinkedList<>();
            lines.add(text.substring(startIndex, endIndex));
            while (endIndex < length) { // 修复超过长度时,最后一行少一个字的问题.
                startIndex = endIndex;
                endIndex = Math.min(startIndex + perLineLength, length);
                lines.add(text.substring(startIndex, endIndex));
            }
            int linesLength = lines.size();
            for (String str : lines) {
                ++line;
                if (DEBUG) {
                    Log.d(TAG, "drawText: str:" + str);
                }
                if (startY < mMiddleY)
                    canvas.drawText(str, mMiddleX, startY - (linesLength - line) * DY, paint);
                else
                    canvas.drawText(str, mMiddleX, startY + (line - 1) * DY, paint);
            }
        } else {
            ++line;
            mPaint.setTextAlign(Paint.Align.CENTER);
            if (DEBUG) {
                Log.d(TAG, "drawText: text:" + text);
            }
            canvas.drawText(text, mMiddleX, startY, paint);
        }
        return line;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (lyric == null) {
            return;
        }

        List<Lyric.Sentence> sentenceList = lyric.sentenceList;
        if (sentenceList == null || sentenceList.isEmpty() || mLyricIndex == -2) {
            return;
        }
        canvas.drawColor(0xEFeffff);

        float currY;

        if (mLyricIndex > -1) {
            // Current line with highlighted color
            currY = getCurrY() + DY * drawText(
                    canvas, mCurrentPaint, sentenceList.get(mLyricIndex).content, getCurrY());
        } else {
            // First line is not from timestamp 0
            currY = getCurrY() + DY;
        }

        // Draw sentences afterwards
        int size = sentenceList.size();
        for (int i = mLyricIndex + 1; i < size; i++) {
            if (currY > mHeight) {
                break;
            }

            // Draw and Move down
            if (i == mLyricIndex + 1) { // 当前的下一行;
                currY += DY * drawText(canvas, mNextPaint, sentenceList.get(i).content, currY);
            } else { // 下一行以外的.
                currY += DY * drawText(canvas, mPaint, sentenceList.get(i).content, currY);
            }
            // canvas.translate(0, DY);
        }

        // 如果是从顶部开始绘制当前行, 之前的歌词就不需要绘制了.
        if (mCurrGravity != TOP) {
            currY = getCurrY() - DY;

            // Draw sentences before current one
            for (int i = mLyricIndex - 1; i >= 0; i--) {
                if (currY < 0) {
                    break;
                }
                // Draw and move upwards
                currY -= DY * drawText(canvas, mPaint, sentenceList.get(i).content, currY);
                // canvas.translate(0, DY);
            }
        }


        if (mIsTouched > 0 && DEBUG) {
            drawMusicInfo(canvas);
        }
    }

    private void drawMusicInfo(Canvas canvas) {
        mPaint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(String.format("%s - %s", lyric.artist, lyric.title), 10, 50, mPaint);
        canvas.drawText("offset: " + lyric.offset, 10, 150, mPaint);
        if (mLyricIndex >= 0) {
            int seconds = (int) ((lyric.sentenceList.get(mLyricIndex).fromTime / 1000));
            int minutes = seconds / 60;
            seconds = seconds % 60;
            canvas.drawText(String.format("%02d:%02d", minutes, seconds), 10, 100, mPaint);
        }
        --mIsTouched;
        mPaint.setTextAlign(Paint.Align.CENTER);
    }


    private float getCurrY() {
        float currY = mMiddleY;
        if (mCurrGravity == TOP) {
            currY = mTextSize;
        }
        return currY;
    }

    protected void onSizeChanged(int w, int h, int ow, int oh) {
        super.onSizeChanged(w, h, ow, oh);
        mMiddleX = w * 0.5f; // remember the center of the screen
        mHeight = h;
        mMiddleY = h * 0.5f;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getActionMasked();
        final boolean superResult = super.onTouchEvent(event);
        if (lyric == null || !mCanDrag) {
            return superResult;
        }

        boolean handled = false;
        boolean offsetChanged = false;

        switch (action) {
            case MotionEvent.ACTION_MOVE:
                mIsTouched = 3;
                float y = event.getY();
                if (mLastEffectY != 0) {
                    if (mLastEffectY - y > 10) {
                        int times = (int) ((mLastEffectY - y) / 10);
                        mLastEffectY = y;
                        lyric.offset += times * -100;
                        offsetChanged = true;
                    } else if (mLastEffectY - y < -10) {
                        int times = -(int) ((mLastEffectY - y) / 10);
                        mLastEffectY = y;
                        lyric.offset += times * 100;
                        offsetChanged = true;
                    }
                }
                handled = true;
                break;
            case MotionEvent.ACTION_DOWN:
                handled = true;
                mLastEffectY = event.getY();
                mIsTouched = 3;
                break;
            case MotionEvent.ACTION_UP:
                System.currentTimeMillis();
                mLastEffectY = 0;
                handled = true;
                break;
            default:
                break;
        }

        if (handled) {
            if (offsetChanged) {
                mNeedUpdate = true;
            }
            return true;
        }

        return superResult;
    }

    /**
     * @param time Timestamp of current sentence
     * @return Timestamp of next sentence, -1 if is last sentence.
     */
    public long updateIndex(long time) {
        // Current index is last sentence
        if (mLyricIndex >= mLyricSentenceLength - 1) {
            mLyricIndex = mLyricSentenceLength - 1;
            return -1;
        }

        // Get index of sentence whose timestamp is between its startTime and currentTime.
        mLyricIndex = LyricUtils.getSentenceIndex(lyric, time, mLyricIndex, lyric.offset);

        // New current index is last sentence
        if (mLyricIndex >= mLyricSentenceLength - 1) {
            mLyricIndex = mLyricSentenceLength - 1;
            return -1;
        }

        return lyric.sentenceList.get(mLyricIndex + 1).fromTime + lyric.offset;
    }

    public synchronized void setLyric(Lyric lyric, boolean resetIndex) {
        this.lyric = lyric;
        mLyricSentenceLength = this.lyric.sentenceList.size();
        if (resetIndex) {
            mLyricIndex = 0;
        }
    }

    public void setLyricIndex(int index) {
        mLyricIndex = index;
    }

    public String getCurrentSentence() {
        if (mLyricIndex >= 0 && mLyricIndex < mLyricSentenceLength) {
            return lyric.sentenceList.get(mLyricIndex).content;
        }
        return null;
    }

    /**
     * Check if view need to update due to user input.
     *
     * @return Whether need update view.
     */
    public boolean checkUpdate() {
        if (mNeedUpdate) {
            mNeedUpdate = false;
            return true;
        }
        return false;
    }

    public synchronized void setLyric(Lyric lyric) {
        setLyric(lyric, true);
    }

    /**
     * 开始展示.
     */
    public void play() {
        if (DEBUG) {
            Log.d(TAG, "play() called");
        }
        mNeedStop = false;
        Thread thread = new Thread(this);
        thread.start();
    }

    /**
     * 重新开始展示.
     */
    public void reStart() {
        if (DEBUG) {
            Log.d(TAG, "reStart() called");
        }
        stop();
        mStartTime = -1;
        mLyricIndex = 0;
        mNextSentenceTime = -1;
        play();
    }

    /**
     * 停止展示.
     */
    public void stop() {
        if (DEBUG) {
            Log.d(TAG, "stop() called");
        }
        mNeedStop = true;
    }

    @Override
    public void run() {
        if (mStartTime == -1) {
            mStartTime = System.currentTimeMillis();
        }

        while (mLyricIndex != -2) {
            if (mNeedStop) {
                mPlaying = false;
                return;
            }
            mPlaying = true;
            long ts = System.currentTimeMillis() - mStartTime;
            if (ts >= mNextSentenceTime || checkUpdate()) {
                mNextSentenceTime = updateIndex(ts);
                if (mOnLyricUpdateListener != null) {
                    mOnLyricUpdateListener.onLyricUpdate();
                }

                // Redraw only when window is visible
                if (mIsForeground) {
                    postInvalidate();
                }
            }
            if (mNextSentenceTime == -1) {
                mNeedStop = true;
            }
        }

        mPlaying = false;
    }

    private int dp2px(int dp) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    /**
     * 是否正在播放.
     * @return
     */
    public boolean isPlaying() {
        return mPlaying;
    }

    /**
     * 设置是否可拖动.
     * <p>默认不可拖动.</p>
     * @param canDrag
     */
    public void setCanDrag(boolean canDrag) {
        mCanDrag = canDrag;
    }

    public interface OnLyricUpdateListener {
        void onLyricUpdate();
    }
}