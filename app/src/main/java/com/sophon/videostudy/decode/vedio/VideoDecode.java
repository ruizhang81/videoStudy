package com.sophon.videostudy.decode.vedio;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.view.Surface;

import com.sophon.videostudy.decode.BaseDecode;

public class VideoDecode extends BaseDecode {

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
            //矫正pts
            sleepRender(info);
            mediaCodec.releaseOutputBuffer(outputId, true);
        }
        // 在所有解码后的帧都被渲染后，就可以停止播放了
        if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            return true;
        }

        return false;
    }



}
