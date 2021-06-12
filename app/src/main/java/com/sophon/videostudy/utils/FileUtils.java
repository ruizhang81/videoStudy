package com.sophon.videostudy.utils;

import android.content.Context;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import androidx.core.content.ContextCompat;

public class FileUtils {

    public static String getPath(Context context) {
        //如果手机已插入sd卡,且app具有读写sd卡的权限
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            try{
//                return "/storage/sdcard0/";
                return Environment.getExternalStorageDirectory().getCanonicalPath();
            }catch (Exception e){e.printStackTrace();}

        } else {
            Toast.makeText(context, "SD卡不存在或者不可读写", Toast.LENGTH_SHORT).show();
        }
        return "sdcard is null";
    }

    public static void savaFileToSD(Context context,String filePath, String filecontent) throws Exception {
        //如果手机已插入sd卡,且app具有读写sd卡的权限
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //这里就不要用openFileOutput了,那个是往手机内存中写数据的
            FileOutputStream output = new FileOutputStream(filePath);
            output.write(filecontent.getBytes());
            //将String字符串以字节流的形式写入到输出流中
            output.close();
            //关闭输出流
        } else Toast.makeText(context, "SD卡不存在或者不可读写", Toast.LENGTH_SHORT).show();
    }

    public static void savaFileToSD(Context context,String filePath, byte[] bytes) throws Exception {
        //如果手机已插入sd卡,且app具有读写sd卡的权限
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            FileOutputStream fileOutputStream = new FileOutputStream(filePath);
            //这里就不要用openFileOutput了,那个是往手机内存中写数据的
            DataOutputStream output = new DataOutputStream(fileOutputStream);
            output.write(bytes);
            output.close();
            //关闭输出流
        } else Toast.makeText(context, "SD卡不存在或者不可读写", Toast.LENGTH_SHORT).show();
    }

    public String readFromSD(String filename) throws IOException {
        StringBuilder sb = new StringBuilder("");
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            filename = Environment.getExternalStorageDirectory().getCanonicalPath() + "/" + filename;
            //打开文件输入流
            FileInputStream input = new FileInputStream(filename);
            byte[] temp = new byte[1024];

            int len = 0;
            //读取文件内容:
            while ((len = input.read(temp)) > 0) {
                sb.append(new String(temp, 0, len));
            }
            //关闭输入流
            input.close();
        }
        return sb.toString();
    }

}
