package com.njcc.util;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.HttpURLConnection;

import java.net.URL;

/**
 * Created by 纯牛奶丶 on 2017/10/19.
 */

public class SPURLConnection {

    private static HttpURLConnection connection = null;

    /**
     *  封装HttpURLConnection的连接
     * @param urlPath
     * @return connection
     * @throws IOException
     */
    public static HttpURLConnection getConnection(String urlPath) throws IOException {
        URL url = new URL(urlPath);
        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(30000);
        return connection;
    }

    /**
     * 关闭流
     * @param is
     * @param os
     * @throws IOException
     */
    public static void close(InputStream is, OutputStream os) throws IOException {
        is.close();
        os.close();
    }

    /**
     * 使用GET方法上传带参数的信息
     * @param urlPath
     * @param params
     */
    public void upload(String urlPath,String params){
        new Thread((Runnable)()->{
            try {
                URL url = new URL(urlPath + params);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();//连接
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(3000);
                connection.setReadTimeout(3000);
                connection.connect();

                int responseCode = connection.getResponseCode();//得到响应码

                if(responseCode == 200){
                    Log.e("***************","OK:"+ responseCode);
                }
                connection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
