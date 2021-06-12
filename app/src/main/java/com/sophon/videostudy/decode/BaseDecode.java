package com.sophon.videostudy.decode;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class BaseDecode implements Runnable {
    protected final static int VIDEO = 1;
    protected final static int AUDIO = 2;
    //等待时间
    protected final static int TIME_US = 10000;
    protected MediaFormat mediaFormat;
    protected MediaCodec mediaCodec;

    private MyExtractor extractor;
    private boolean isDone;


    public BaseDecode(String path,Surface surface) {
        try {
            //获取 MediaExtractor
            extractor = new MyExtractor(path);
            //判断是音频还是视频
            int type = decodeType();
            //拿到音频或视频的 MediaFormat
            mediaFormat = (type == VIDEO ? extractor.videoFormat : extractor.audioFormat);

            String mime = mediaFormat.getString(MediaFormat.KEY_MIME);
            //选择要解析的轨道
            extractor.selectTrack(type == VIDEO ? extractor.videoTrackId : extractor.audioTrackId);
            //创建 MediaCodec
            mediaCodec = MediaCodec.createDecoderByType(mime);
            //由子类去配置
            configure(surface);
            mediaCodec.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected abstract void configure(Surface surface);


    public abstract int decodeType();


    @Override
    public void run() {
        try {
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            //编码
            while (!isDone) {
                int inputBufferId = mediaCodec.dequeueInputBuffer(TIME_US);

                if (inputBufferId > 0) {
                    ByteBuffer inputBuffer = mediaCodec.getInputBuffer(inputBufferId);
                    if (inputBuffer != null) {
                        int size = extractor.readBuffer(inputBuffer);//下一
                        //解析数据
                        if (size >= 0) {
                            mediaCodec.queueInputBuffer(
                                    inputBufferId,
                                    0,
                                    size,
                                    extractor.curSampleTime,
                                    extractor.curSampleFlags
                            );
                        } else {
                            //结束,传递 end-of-stream 标志
                            mediaCodec.queueInputBuffer(
                                    inputBufferId,
                                    0,
                                    0,
                                    0,
                                    MediaCodec.BUFFER_FLAG_END_OF_STREAM
                            );
                            isDone = true;

                        }
                    }
                }
                //解码输出交给子类
                boolean isFinish = handleOutputData(info);
                if (isFinish) {
                    break;
                }

            }

            stop();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            isDone = true;
            //释放 mediacodec
            mediaCodec.stop();
            mediaCodec.release();

            //释放 MediaExtractor
            extractor.release();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    protected abstract boolean handleOutputData(MediaCodec.BufferInfo info);


}
