package com.njcc.main;

import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.njcc.util.SPURLConnection;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;

public class PayActivity extends AppCompatActivity {

    String money = null;//支付金额
    int count = 0;//支付次数
    private TextView tv_money = null;//显示支付金额
    private ImageView btn_pay = null;//确认支付按钮

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);

        tv_money = (TextView) findViewById(R.id.tv_money);
        btn_pay = (ImageView) findViewById(R.id.btn_pay);

        //获取从PayActivity传来的支付金额
        Intent intent = getIntent();
        money = intent.getStringExtra("money");
        tv_money.setText(money + "元");

        btn_pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //第一次支付时count==0，支付之后count==1；当count!=0时，不可以点击支付
                if(count == 0){
                    getPayment();
                    count+=1;
                }else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new AlertDialog.Builder(PayActivity.this).setTitle("提示！").setMessage("支付成功！请勿重新支付！").setPositiveButton(null,null).show();
                        }
                    });
                    btn_pay.setEnabled(false);//设置支付按钮不可点击
                }
            }
        });
    }

    /**
     * 使用post请求发送支付请求
     */
    public void getPayment(){
        new Thread((Runnable)()->{
            String urlPath = getString(R.string.url_upload_money);
            try {
                HttpURLConnection connection = SPURLConnection.getConnection(urlPath);
                connection.connect();
                OutputStream os = connection.getOutputStream();
                os.write(money.getBytes("utf-8"));//截取金额数为纯数字

                /**
                 * 得到服务器的支付结果信息
                 */
                InputStream is = connection.getInputStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] b = new byte[1024];
                int len = 0;
                while((len = is.read(b)) != -1){
                    baos.write(b, 0, len);
                }

                String ok = baos.toString();
                if(ok.equals("ok")){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new AlertDialog.Builder(PayActivity.this).setTitle("完成").setMessage("支付成功！正在打印！").setPositiveButton(null,null).show();
                        }
                    });
                }
                //关闭流和网络连接
                baos.close();
                is.close();
                os.close();
                connection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(PayActivity.this, MainActivity.class));
        return;
    }
}
