package cn.zhaiyifan.lyric;

import cn.zhaiyifan.lyric.model.Lyric;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by qinglian on 17/4/10.
 */

public class LyricUtilsTest {


    @org.junit.Test
    public void testParseLyric() throws Exception {
        String lrc = "[00:15.25]我听见雨滴落在青青草地\r\n[00:21.57]我听见远方下课钟声响起\r\n[00:27.58]可是我没有听见你的声音\r\n[00:32.60]认真 呼唤我姓名\r\n[00:39.68]爱上你的时候还不懂感情\r\n[00:45.90]离别了才觉得刻骨 铭心\r\n[00:51.95]为什么没有发现遇见了你\r\n[00:56.52]是生命最好的事情\r\n[01:02.28]也许当时忙着微笑和哭泣\r\n[01:08.19]忙着追逐天空中的流星\r\n[01:13.96]人理所当然的忘记\r\n[01:18.57]是谁风里雨里一直默默守护在原地\r\n[01:26.21]原来你是我最想留住的幸运\r\n[01:31.53]原来我们和爱情曾经靠得那么近\r\n[01:37.60]那为我对抗世界的决定\r\n[01:42.18]那陪我淋的雨\r\n[01:45.24]一幕幕都是你 一尘不染的真心\r\n[01:52.49]与你相遇 好幸运\r\n[01:55.80]可我已失去为你泪流满面的权利\r\n[02:01.83]但愿在我看不到的天际\r\n[02:06.50]你张开了双翼\r\n[02:09.48]遇见你的注定 她会有多幸运\r\n[02:28.98]青春是段跌跌撞撞的旅行\r\n[02:35.14]拥有着后知后觉的美丽\r\n[02:40.79]来不及感谢是你给我勇气\r\n[02:45.99]让我能做回我自己\r\n[02:51.57]也许当时忙着微笑和哭泣\r\n[02:57.64]忙着追逐天空中的流星\r\n[03:03.37]人理所当然的忘记\r\n[03:07.97]是谁风里雨里一直默默守护在原地\r\n[03:15.45]原来你是我最想留住的幸运\r\n[03:20.90]原来我们和爱情曾经靠得那么近\r\n[03:27.03]那为我对抗世界的决定\r\n[03:31.65]那陪我淋的雨\r\n[03:34.48]一幕幕都是你 一尘不染的真心\r\n[03:41.92]与你相遇 好幸运\r\n[03:45.10]可我已失去为你泪流满面的权利\r\n[03:51.28]但愿在我看不到的天际\r\n[03:55.84]你张开了双翼\r\n[03:58.84]遇见你的注定\r\n[04:02.25]oh 她会有多幸运\r\n";
        Lyric lyric = LyricUtils.parseLyric(lrc);
        assertNotNull(lyric);
        System.out.println(lyric);
        assertNotNull(lyric.sentenceList);
        assertEquals(lyric.sentenceList.size() > 0, true);
    }

}