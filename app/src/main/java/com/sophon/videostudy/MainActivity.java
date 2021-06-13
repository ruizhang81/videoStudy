package com.sophon.videostudy;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import com.sophon.videostudy.decode.audio.AudioDecode;
import com.sophon.videostudy.decode.BaseDecode;
import com.sophon.videostudy.decode.vedio.VideoDecode;
import com.sophon.videostudy.utils.FileUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {

    private final int REQUEST = 1;
    private SurfaceView surfaceView;
    public String TAG = "rui";
    public List<BaseDecode> baseDecodeList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        surfaceView = findViewById(R.id.sfv);

        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS}
                , REQUEST);

        init();
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            Toast.makeText(MainActivity.this, "Permission GET", Toast.LENGTH_SHORT).show();

        } else {
//             Permission Denied
            Toast.makeText(MainActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    private void init() {
        String dirPath = FileUtils.getPath(this);
        Log.i(TAG, "dirPath " + dirPath);

        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                baseDecodeList.clear();
                ExecutorService mExecutorService = Executors.newFixedThreadPool(2);
                VideoDecode videoDecode = new VideoDecode(dirPath + "/test.mp4",holder.getSurface());
                AudioDecode audioEncode = new AudioDecode(dirPath + "/test.mp4",null);
                baseDecodeList.add(videoDecode);
                baseDecodeList.add(audioEncode);
                mExecutorService.execute(videoDecode);
                mExecutorService.execute(audioEncode);
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                for(BaseDecode baseDecode:baseDecodeList){
                    baseDecode.stop();
                }
            }
        });
        surfaceView.setVisibility(View.VISIBLE);
    }
}