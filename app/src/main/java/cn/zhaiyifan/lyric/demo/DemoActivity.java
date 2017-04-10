package cn.zhaiyifan.lyric.demo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import cn.zhaiyifan.lyric.LyricUtils;
import cn.zhaiyifan.lyric.widget.LyricView;

public class DemoActivity extends Activity implements View.OnClickListener {

    private LyricView mLyricView;
    private Button mBtnRestart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLyricView = (LyricView) findViewById(R.id.lyricView);
        mBtnRestart = (Button) findViewById(R.id.btn_restart);
        mLyricView.setLyric(LyricUtils.parseLyric(
                getResources().openRawResource(R.raw.testfile), "UTF-8"));
        mLyricView.setLyricIndex(0);
        mLyricView.play();

        mBtnRestart.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int vid = v.getId();
        if (vid == R.id.btn_restart) {
            mLyricView.reStart();
        }
    }
}