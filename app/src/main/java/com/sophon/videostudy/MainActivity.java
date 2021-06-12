package com.sophon.videostudy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.sophon.videostudy.utils.FileUtils;
import com.sophon.videostudy.decode.vedio.VideoDecode;

public class MainActivity extends AppCompatActivity {

    private final int REQUEST = 1;
    VideoDecode videoDecode;
    public String TAG = "rui";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


         //REQUEST_GPS为自定义int型静态常量；private final int REQUEST_GPS = 1;
              ActivityCompat.requestPermissions(MainActivity.this,
                       new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                               Manifest.permission.READ_EXTERNAL_STORAGE,
                                      Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS}
                      ,REQUEST);

        String dirPath = FileUtils.getPath(this);
        Log.i(TAG,"dirPath "+dirPath);

        SurfaceView surfaceView  = findViewById(R.id.surfaceView);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                videoDecode = new VideoDecode(dirPath + "/test.mp4");
                videoDecode.setSurface(holder.getSurface());
                videoDecode.start();
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                videoDecode.stop();
            }
        });

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

}