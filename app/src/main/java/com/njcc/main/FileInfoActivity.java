package com.njcc.main;

import android.content.Intent;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.njcc.model.FileInfo;

public class FileInfoActivity extends AppCompatActivity {

    private TextView tv_name = null;//文件名
    private TextView tv_type = null;//文件类型
    private TextView tv_pages = null;//文件页数
    private TextView tv_cost = null;//支付价格
    private ImageView btn_back = null;//返回键

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_info);

        tv_name = (TextView) findViewById(R.id.tv_name);
        tv_type = (TextView) findViewById(R.id.tv_type);
        tv_pages = (TextView) findViewById(R.id.tv_pages);
        tv_cost = (TextView) findViewById(R.id.tv_cost);
        btn_back = (ImageView) findViewById(R.id.btn_back);

        /**
         * 得到MainActivity传过来的文件信息对象fileInfo
         */
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        FileInfo fileInfo = (FileInfo) bundle.get("fileInfo");
        String name = fileInfo.getName();
        String type = fileInfo.getKind();
        int pages = fileInfo.getPages();
        double cost = fileInfo.getCost();

        setFileInfo(name, type, pages, cost);//执行显示文件信息方法
    }

    /**
     * 跳转至支付页面，并将支付价格传递至PayActivity
     */
    public void toPay(View v){
        String money = tv_cost.getText().toString();
        //MainActivity.this.startActivity(new Intent( MainActivity.this , PayActivity.class));
        Intent intent = new Intent(FileInfoActivity.this , PayActivity.class);
        intent.putExtra("money",money.substring(0, money.length()-1));
        startActivity(intent);
    }

    /**
     * 点击关闭返回上一页
     * @param v
     */
    public void toBack(View v){
        finish();
    }


    /**
     * 在页面上显示文件名，文件类型，文件页数，需要支付的费用
     * @param name
     * @param type
     * @param pages
     * @param cost
     */
    private void setFileInfo(String name , String type, int pages, double cost) {
        runOnUiThread((Runnable)()->{
            tv_name.setText(name);
            tv_type.setText(type + "文件");
            tv_pages.setText(pages + "页");
            tv_cost.setText(cost + "元");
        });
    }

}
