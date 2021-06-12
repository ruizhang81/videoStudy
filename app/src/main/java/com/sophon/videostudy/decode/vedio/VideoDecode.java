package com.sophon.videostudy.decode.vedio;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.view.Surface;

import com.sophon.videostudy.decode.BaseDecode;

public class VideoDecode extends BaseDecode {


    // 用于对准视频的时间戳
    private long startMs = -1;

    public VideoDecode(String path,Surface surface) {
        super(path,surface);
    }

    @Override
    protected void configure(Surface surface) {
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 15);
        mediaCodec.configure(mediaFormat, surface, null, 0);
    }

    @Override
    public int decodeType() {
        return VIDEO;
    }

    protected boolean handleOutputData(MediaCodec.BufferInfo info) {
        //等到拿到输出的buffer下标
        int outputId = mediaCodec.dequeueOutputBuffer(info, TIME_US);
        if (outputId >= 0) {
            if (startMs == -1) {
                startMs = System.currentTimeMillis();
            }
            //矫正pts
            sleepRender(info, startMs);
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
