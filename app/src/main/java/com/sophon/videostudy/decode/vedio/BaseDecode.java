package com.sophon.videostudy.decode.vedio;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.provider.SyncStateContract;

import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class BaseDecode implements Runnable{
    final static int VIDEO = 1;
    final static int AUDIO = 2;
    //等待时间
    final static int TIME_US = 1000;
    MediaFormat mediaFormat;
    MediaCodec mediaCodec;
    MyExtractor extractor;
    private boolean isDone;

    public BaseDecode(String path) {
        try {
            //获取 MediaExtractor
            extractor = new MyExtractor(path);
            //判断是音频还是视频
            int type = decodeType();
            //拿到音频或视频的 MediaFormat
            mediaFormat = (type == VIDEO ? extractor.getVideoFormat() : extractor.getAudioFormat());
            String mime = mediaFormat.getString(MediaFormat.KEY_MIME);
            //选择要解析的轨道
            extractor.selectTrack(type == VIDEO ? extractor.getVideoTrackId() : extractor.getAudioTrackId());
            //创建 MediaCodec
            mediaCodec = MediaCodec.createDecoderByType(mime);
            //由子类去配置
            configure();
            //开始工作，进入编解码状态
            mediaCodec.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected abstract void configure();


    public abstract int decodeType();


    @Override
    public void run() {
        try {
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            //编码
            while (!isDone) {
                /**
                 * 延迟 TIME_US 等待拿到空的 input buffer下标，单位为 us
                 * -1 表示一直等待，知道拿到数据，0 表示立即返回
                 */
                int inputBufferId = mediaCodec.dequeueInputBuffer(TIME_US);

                if (inputBufferId > 0) {
                    //拿到 可用的，空的 input buffer
                    ByteBuffer inputBuffer = mediaCodec.getInputBuffer(inputBufferId);
                    if (inputBuffer != null) {
                        /**
                         * 通过 mediaExtractor.readSampleData(buffer, 0) 拿到视频的当前帧的buffer
                         * 通过 mediaExtractor.advance() 拿到下一帧
                         */
                        int size = extractor.readBuffer(inputBuffer);
                        //解析数据
                        if (size >= 0) {
                            mediaCodec.queueInputBuffer(
                                    inputBufferId,
                                    0,
                                    size,
                                    extractor.getSampleTime(),
                                    extractor.getSampleFlags()
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
                boolean isFinish =  handleOutputData(info);
                if (isFinish){
                    break;
                }

            }

            stop();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop(){
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

    abstract boolean handleOutputData(MediaCodec.BufferInfo info);
}
