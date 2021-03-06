package com.sophon.videostudy.decode.audio;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Surface;

import com.sophon.videostudy.decode.BaseDecode;
import com.sophon.videostudy.decode.MyExtractor;

import java.io.IOException;
import java.nio.ByteBuffer;

import androidx.annotation.NonNull;

public class AudioDecode extends BaseDecode {

    private int mPcmEncode;
    //一帧的最小buffer大小
    private final int minBufferSize;
    private AudioTrack audioTrack;

    public AudioDecode(String path,Surface surface) {
        super(path,surface);
        //拿到采样率
        if (mediaFormat.containsKey(MediaFormat.KEY_PCM_ENCODING)) {
            mPcmEncode = mediaFormat.getInteger(MediaFormat.KEY_PCM_ENCODING);
        } else {
            //默认采样率为 16bit
            mPcmEncode = AudioFormat.ENCODING_PCM_16BIT;
        }

        //音频采样率
        int sampleRate = mediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
        //获取视频通道数
        int channelCount = mediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);

        //拿到声道
        int channelConfig = channelCount == 1 ? AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_IN_STEREO;
        minBufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, mPcmEncode);


        /**
         * 设置音频信息属性
         * 1.设置支持多媒体属性，比如audio，video
         * 2.设置音频格式，比如 music
         */
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
        /**
         * 设置音频数据
         * 1. 设置采样率
         * 2. 设置采样位数
         * 3. 设置声道
         */
        AudioFormat format = new AudioFormat.Builder()
                .setSampleRate(sampleRate)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(channelConfig)
                .build();


        //配置 audioTrack
        audioTrack = new AudioTrack(
                attributes,
                format,
                minBufferSize,
                AudioTrack.MODE_STREAM,
                AudioManager.AUDIO_SESSION_ID_GENERATE
        );
//        audioTrack.setVolume(2f);
//        audioTrack.setPlaybackRate(10);
        //监听播放
        audioTrack.play();
    }

    @Override
    protected void configure(Surface surface) {

//        mediaFormat.setInteger(MediaFormat.KEY_IS_ADTS, 1);
//        byte[] data = new byte[]{(byte) 0x14, (byte) 0x08};
//        ByteBuffer csd_0 = ByteBuffer.wrap(data);
//        mediaFormat.setByteBuffer("csd-0", csd_0);

        mediaCodec.configure(mediaFormat, null, null, 0);
    }

    @Override
    public int decodeType() {
        return AUDIO;
    }


    @Override
    protected boolean handleOutputData(MediaCodec.BufferInfo info) {
        //拿到output buffer
        int outputIndex = mediaCodec.dequeueOutputBuffer(info, TIME_US);
        ByteBuffer outputBuffer;
        if (outputIndex >= 0) {

            //矫正pts
            sleepRender(info);

            outputBuffer = mediaCodec.getOutputBuffer(outputIndex);
            //写数据到 AudioTrack 只，实现音频播放
            audioTrack.write(outputBuffer, info.size, AudioTrack.WRITE_BLOCKING);
            mediaCodec.releaseOutputBuffer(outputIndex, false);
        }
        // 在所有解码后的帧都被渲染后，就可以停止播放了
        if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            return true;
        }
        return false;
    }

}
