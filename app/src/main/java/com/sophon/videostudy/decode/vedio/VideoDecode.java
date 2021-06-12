package com.sophon.videostudy.decode.vedio;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

public class VideoDecode extends BaseDecode{

    private Surface surface;
    String path;
    long TIME_US = 10000;
    MyExtractor extractor;

    public VideoDecode(String path) {
        super(path);
    }

    public void setSurface(Surface surface){
        this.surface = surface;
    }

    @Override
    protected void configure() {
        extractor = new MyExtractor(path);
        //创建视频格式信息
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE,15);
        //！！！注意，这行代码需要界面绘制完成之后才可以调用！！！
        mediaCodec.configure(mediaFormat, surface, null, 0);
    }

    @Override
    public int decodeType() {
        return VIDEO;
    }

    public void start() {
        mediaCodec.start();
        start();
    }

    // 用于对准视频的时间戳
    private long startMs = -1;

    boolean handleOutputData(MediaCodec.BufferInfo info) {
        //等到拿到输出的buffer下标
        int outputId = mediaCodec.dequeueOutputBuffer(info, TIME_US);

        if (outputId >= 0){
            if (startMs == -1) {
                startMs = System.currentTimeMillis();
            }
            //矫正pts
            sleepRender(info, startMs);
            //释放buffer，并渲染到 Surface 中
            //释放buffer，并渲染到 Surface 中
            mediaCodec.releaseOutputBuffer(outputId, true);
        }

        // 在所有解码后的帧都被渲染后，就可以停止播放了
        if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            return true;
        }

        return false;
    }

    /**
     * 数据的时间戳对齐
     **/
    private void sleepRender(MediaCodec.BufferInfo info, long startMs) {
        /**
         * 注意这里是以 0 为出事目标的，info.presenttationTimes 的单位为微秒
         * 这里用系统时间来模拟两帧的时间差
         */
        long ptsTimes = info.presentationTimeUs / 1000;
        long systemTimes = System.currentTimeMillis() - startMs;
        long timeDifference = ptsTimes - systemTimes;
        // 如果当前帧比系统时间差快了，则延时以下
        if (timeDifference > 0) {
            try {
                Thread.sleep(timeDifference);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

}
