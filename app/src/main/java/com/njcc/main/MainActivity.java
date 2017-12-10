package com.njcc.main;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.njcc.model.FileInfo;
import com.njcc.util.SPURLConnection;
import com.njcc.util.UriToPathUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    public static final int REQUEST_CODE = 200;//请求码，200
    public static final int RESPONSE_CODE_OK = 200;//服务端响应码200，请求成功
    public static final int RESPONSE_CODE_ERROR = 500;//服务端响应码500，失败
    static String choose_path = "";//选择的文件的路径

    private EditText text_choose = null;//选择文本框
    private ImageView btn_upload = null;//计算支付价钱
    private TextView tv_upload = null;//显示文字
    private ProgressBar pb_progressBar = null;//进度条

    int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPerm();//动态获取存储权限

        text_choose = (EditText) findViewById(R.id.text_choose);
        btn_upload = (ImageView) findViewById(R.id.btn_upload);
        tv_upload = (TextView) findViewById(R.id.tv_upload);
        pb_progressBar = (ProgressBar) findViewById(R.id.pb_progressBar);

        text_choose.setOnClickListener(this);
        btn_upload.setOnClickListener(this);

        /**
         * 设置text_choose文本框不可输入
         */
        text_choose.setFocusable(false);
        text_choose.setFocusableInTouchMode(false);

        /**
         * 设置初始时上传提示和进度条不可显示
         */
        tv_upload.setVisibility(View.INVISIBLE);
        pb_progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onClick(View view) {
        boolean isChosen = text_choose.getText().toString().trim().equals("");//是否已经选择文件，没选择则为true，默认没有选择

        switch (view.getId()){
            case R.id.text_choose:
                chooseFile();
                break;
            case R.id.btn_upload:
                if(!isChosen){
                    //如果选择了文件，则可以执行上传
                    uploadFile();
                    //在主线程显示上传提示的进度条
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tv_upload.setVisibility(View.VISIBLE);
                            pb_progressBar.setVisibility(View.VISIBLE);
                           new Thread(new Runnable() {
                               @Override
                               public void run() {
                                   while(count<9){
                                       count++;
                                       try {
                                           Thread.sleep(200);
                                       } catch (InterruptedException e) {
                                           e.printStackTrace();
                                       }
                                       pb_progressBar.setProgress(count);
                                   }
                               }
                           }).start();
                        }
                    });
                }else{
                    /**
                     * 没有选择文件，弹框显示先选择文件
                     */
                   new AlertDialog.Builder(this).setTitle("提示！").setMessage("请先选择要打印的文件！").setPositiveButton("选择", new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface dialogInterface, int i) {
                           //执行选择文件
                           chooseFile();
                       }
                   }).show();
                }
                break;
        }
    }

    /**
     * 调用本地资源文件管理器选择文件
     */
    public void chooseFile(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");//设置类型，这里是任意类型
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent,1);
    }

    /**
     * 将本地选中的文件上传到服务器，接受服务器的响应码，并更改在主线程更新数据。
     */
    public void uploadFile() {
        File file = new File(choose_path);//获取文件对象
        new Thread(new Runnable() {
            @Override
            public void run() {
                String urlPath = getString(R.string.url_upload);//获取上传地址
                Log.e("urlPath",urlPath);
                OutputStream os = null;
                FileInputStream is = null;
                HttpURLConnection connection = null;
                try {
                    connection = SPURLConnection.getConnection(urlPath);//创建connection对象
                    connection.connect();
                    os = connection.getOutputStream();//得到输出流
                    //is = getAssets().open("Spring_表达式语言.pdf");//获取assets文件中文件
                    is = new FileInputStream(file);
                    byte[] b = new byte[1024];
                    int len = 0;
                    while ((len = is.read(b)) != -1) {
                        os.write(b, 0, len);
                    }

                    int responseCode = connection.getResponseCode();//拿到响应码
                    if (RESPONSE_CODE_OK == responseCode) {
                        InputStream inputStream = connection.getInputStream();//得到输入流
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024];
                        int l = 0;
                        while ((l = inputStream.read(buffer)) != -1) {
                            baos.write(buffer, 0, l);
                        }

                        //将获取的输入流转换成json对象，并转换为FileInfo对象
                        Gson gson = new Gson();
                        FileInfo fileInfo = gson.fromJson(baos.toString(), FileInfo.class);

                        //在收到fileInfo对象后自动在主线程中将fileInfo对象传到FileInfoActivity
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //设置上传文件和进度条不可显示
                                pb_progressBar.setProgress(10);
                                tv_upload.setVisibility(View.INVISIBLE);
                                pb_progressBar.setVisibility(View.INVISIBLE);

                                Intent intent = new Intent(MainActivity.this, FileInfoActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("fileInfo", fileInfo);//将文件信息传递到FileInfoActivity
                                intent.putExtras(bundle);

                                startActivity(intent);
                            }
                        });
                        //关闭流
                        baos.close();
                        inputStream.close();
                    }else if(RESPONSE_CODE_ERROR == responseCode){
                        //如果服务端没有成功接受文件，请求客户端重新选择文件发送
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                new AlertDialog.Builder(MainActivity.this).setTitle("提示！").setMessage("请重新选择要打印的文件！").setPositiveButton("选择", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        chooseFile();
                                    }
                                }).show();
                            }
                        });
                    }else{
                       runOnUiThread(new Runnable() {
                           @Override
                           public void run() {
                               tv_upload.setText("解析失败，请重新选择上传！");
                               pb_progressBar.setProgress(10);
                               Toast.makeText(MainActivity.this,responseCode+"ERROR!",Toast.LENGTH_SHORT).show();
                           }
                       });
                    }
                } catch (IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tv_upload.setText("网络错误，上传失败！");
                            pb_progressBar.setProgress(0);
                            Toast.makeText(MainActivity.this,"IO,Error!",Toast.LENGTH_SHORT).show();
                        }
                    });
                    e.printStackTrace();
                } finally {
                    try {
                        SPURLConnection.close(is,os);//关闭流
                        connection.disconnect();//关闭连接
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }


    /**
     * 在回掉函数时拿到将要上传文件的路径，并且上传文件名，且在页面上显示文件名称
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //是否选择，没选择就不会继续
        if (resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();//得到uri，后面就是将uri转化成file的过程。
            choose_path = UriToPathUtil.getRealFilePath(this,uri);//拿到被选择的文件的路径
            String[] b = choose_path.split("\\/");//用正则分离文件夹
            String fileName = b[b.length-1];//得到文件名
            text_choose.setText(fileName);//在text_choose上显示文件名称
            new SPURLConnection().upload(getString(R.string.url_fileName),"?fileName="+fileName);//将文件名上传到服务器
        }
    }

    private void checkPerm() {
        /**1.在AndroidManifest文件中添加需要的权限。
         * 2.检查权限
         *这里涉及到一个API，ContextCompat.checkSelfPermission，
         * 主要用于检测某个权限是否已经被授予，方法返回值为PackageManager.PERMISSION_DENIED
         * 或者PackageManager.PERMISSION_GRANTED。当返回DENIED就需要进行申请授权了。
         * */
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) { //权限没有被授予
            /**3.申请授权
             * @param
             *  @param activity The target activity.（Activity|Fragment、）
             * @param permissions The requested permissions.（权限字符串数组）
             * @param requestCode Application specific request code to match with a result（int型申请码）
             *    reported to {@link OnRequestPermissionsResultCallback#onRequestPermissionsResult(
             *int, String[], int[])}.
             * */
            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE }, REQUEST_CODE);
        }
    }

}