package com.sophon.videostudy.decode.vedio;

import android.media.MediaExtractor;
import android.media.MediaFormat;

import java.io.IOException;
import java.nio.ByteBuffer;

public class MyExtractor {

    private MediaFormat audioFormat;
    private int audioTrackId;
    private int videoTrackId;
    MediaFormat videoFormat;
    MediaExtractor mediaExtractor;
    private long curSampleTime;
    int curSampleFlags ;

    public int getSampleFlags() {
        return curSampleFlags;
    }

    public long getSampleTime() {
        return curSampleTime;
    }

    public MyExtractor(String path) {
        try {
            mediaExtractor = new MediaExtractor();
            // 设置数据源
            mediaExtractor.setDataSource(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //拿到所有的轨道
        int count = mediaExtractor.getTrackCount();
        for (int i = 0; i < count; i++) {
            //根据下标拿到 MediaFormat
            MediaFormat format = mediaExtractor.getTrackFormat(i);
            //拿到 mime 类型
            String mime = format.getString(MediaFormat.KEY_MIME);
            //拿到视频轨
            if (mime.startsWith("video")) {
                videoTrackId = i;
                videoFormat = format;
            } else if (mime.startsWith("audio")) {
                //拿到音频轨
                audioTrackId = i;
                audioFormat = format;
            }

        }
    }

    public void release(){
        mediaExtractor.release();
    }


    public void selectTrack(int trackId){
        mediaExtractor.selectTrack(trackId);
    }

    /**
     * 读取一帧的数据
     *
     * @param buffer
     * @return
     */
    public int readBuffer(ByteBuffer buffer) {
        //先清空数据
        buffer.clear();
        //选择要解析的轨道
//          mediaExtractor.selectTrack(video ? videoTrackId : audioTrackId);
        mediaExtractor.selectTrack(videoTrackId);
        //读取当前帧的数据
        int buffercount = mediaExtractor.readSampleData(buffer, 0);
        if (buffercount < 0) {
            return -1;
        }
        //记录当前时间戳
        curSampleTime = mediaExtractor.getSampleTime();
        //记录当前帧的标志位
        curSampleFlags = mediaExtractor.getSampleFlags();
        //进入下一帧
        mediaExtractor.advance();
        return buffercount;
    }

    public MediaFormat getVideoFormat() {
        return videoFormat;
    }

    public MediaFormat getAudioFormat() {
        return audioFormat;
    }

    public int getVideoTrackId() {
        return videoTrackId;
    }

    public int getAudioTrackId() {
        return audioTrackId;
    }


}
