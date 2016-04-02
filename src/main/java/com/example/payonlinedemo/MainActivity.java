package com.example.payonlinedemo;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RelativeLayout;

import com.google.gson.Gson;
import com.pingplusplus.android.PaymentActivity;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private static final int REQUEST_CODE_PAYMENT = 1;

    /**
     * 银联支付渠道
     */
    private static final String CHANNEL_UPACP = "upacp";
    /**
     * 微信支付渠道
     */
    private static final String CHANNEL_WECHAT = "wx";
    /**
     * 支付支付渠道
     */
    private static final String CHANNEL_ALIPAY = "alipay";
    /**
     * 百度支付渠道
     */
    private static final String CHANNEL_BFB = "bfb";
    /**
     * 京东支付渠道
     */
    private static final String CHANNEL_JDPAY_WAP = "jdpay_wap";

    /**
     * 阿里支付
     */
    private RelativeLayout mLayoutAlipay;

    /**
     * 微信支付
     */
    private RelativeLayout mLayoutWechat;

    /**
     * 百度钱包支付
     */
    private RelativeLayout mLayoutBd;

    //阿里RadioButton
    private RadioButton mRbAlipay;
    //微信RadioButton
    private RadioButton mRbWechat;
    //百度钱包RadioButton
    private RadioButton mRbBd;


    private HashMap<String, RadioButton> chanels = new HashMap<>(3);

    //默认是阿里支付
    private String payChannel = CHANNEL_ALIPAY;

    //模拟的服务器端接口
    private static final String URL = "http://192.168.1.109:8080/Pingxx/PayServlet";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pay_item);
        initControl();
        init();
    }


    private void init() {

        //把三种支付方式的Tag加入到数组中
        chanels.put(CHANNEL_ALIPAY, mRbAlipay);
        chanels.put(CHANNEL_WECHAT, mRbWechat);
        chanels.put(CHANNEL_BFB, mRbBd);


        /**
         * 对三种支付方式的RadioButton实现点击监听
         */
        mLayoutAlipay.setOnClickListener(this);
        mLayoutWechat.setOnClickListener(this);
        mLayoutBd.setOnClickListener(this);

    }


    /**
     * 选择支付方式，在这里对RadioButton进行了只能选择一个，其余的均为不选择情况
     *
     * @param paychannel
     */
    public void selectPayChannel(String paychannel) {
        payChannel = paychannel;
        for (Map.Entry<String, RadioButton> entry : chanels.entrySet()) {
            RadioButton rb = entry.getValue();
            if (entry.getKey().equals(payChannel)) {
                boolean isCheck = rb.isChecked();
                rb.setChecked(!isCheck);
                new PaymentTask().execute(new PaymentRequest(paychannel, 100));
            } else
                rb.setChecked(false);
        }
    }


    /**
     * 初始化布局控件
     */
    private void initControl() {
        mLayoutAlipay = (RelativeLayout) findViewById(R.id.rl_alipay);
        mLayoutWechat = (RelativeLayout) findViewById(R.id.rl_wechat);
        mLayoutBd = (RelativeLayout) findViewById(R.id.rl_bd);
        mRbAlipay = (RadioButton) findViewById(R.id.rb_alipay);
        mRbWechat = (RadioButton) findViewById(R.id.rb_webchat);
        mRbBd = (RadioButton) findViewById(R.id.rb_bd);
    }

    @Override
    public void onClick(View v) {
        selectPayChannel(v.getTag().toString());
    }

    //异步支付任务
    class PaymentTask extends AsyncTask<PaymentRequest, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
           /* //防止重复提交
            mLayoutAlipay.setOnClickListener(null);
            mLayoutWechat.setOnClickListener(null);
            mLayoutBd.setOnClickListener(null);*/
        }

        @Override
        protected String doInBackground(PaymentRequest... params) {
            PaymentRequest paymentRequest = params[0];
            String data = null;
            //转化为json数据
            String json = new Gson().toJson(paymentRequest);
            try {
                //向Your Ping++ Server SDK请求数据
                //URL  服务器地址
                data = postJson(URL, json);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return data;
        }

        /**
         * 获得服务端的charge，调用ping++ sdk。
         */
        @Override
        protected void onPostExecute(String data) {
            if (null == data) {
                showMsg("请求出错", "请检查URL", "URL无法获取charge");
                return;
            }
            postNewOrder(data);
        }
    }

    private void postNewOrder(String data) {

        String packageName = getPackageName();
        ComponentName componentName = new ComponentName(packageName, packageName + ".wxapi.WXPayEntryActivity");
        Intent intent = new Intent(MainActivity.this, PaymentActivity.class);
        intent.setComponent(componentName);
        intent.putExtra(PaymentActivity.EXTRA_CHARGE, data);
        startActivityForResult(intent, REQUEST_CODE_PAYMENT);

    }


    /**
     * onActivityResult 获得支付结果，如果支付成功，服务器会收到ping++ 服务器发送的异步通知。
     * 最终支付成功根据异步通知为准
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        mLayoutAlipay.setOnClickListener(this);
        mLayoutWechat.setOnClickListener(this);
        mLayoutBd.setOnClickListener(this);

        //支付页面返回处理
        if (requestCode == REQUEST_CODE_PAYMENT) {
            if (resultCode == Activity.RESULT_OK) {
                String result = data.getExtras().getString("pay_result");
                /* 处理返回值
                 * "success" - payment succeed
                 * "fail"    - payment failed
                 * "cancel"  - user canceld
                 * "invalid" - payment plugin not installed
                 */
                String errorMsg = data.getExtras().getString("error_msg"); // 错误信息
                String extraMsg = data.getExtras().getString("extra_msg"); // 错误信息
                showMsg(result, errorMsg, extraMsg);
            }
        }
    }


    /**
     * 显示信息
     *
     * @param title
     * @param msg1
     * @param msg2
     */
    public void showMsg(String title, String msg1, String msg2) {
        String str = title;
        if (null != msg1 && msg1.length() != 0) {
            str += "\n" + msg1;
        }
        if (null != msg2 && msg2.length() != 0) {
            str += "\n" + msg2;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(str);
        builder.setTitle("提示");
        builder.setPositiveButton("OK", null);
        builder.create().show();
    }


    /**
     * 解析数据
     *
     * @param url
     * @param json
     * @return
     * @throws IOException
     */
    private static String postJson(String url, String json) throws IOException {
        //表示发送的数据格式是json   编码是utf-8
        //response.setContentType("application/json;charset=UTF-8");
        MediaType type = MediaType.parse("application/json;charset=UTF-8");
        //请求主体
        RequestBody body = RequestBody.create(type, json);
        //请求构建  封装请求对象
        Request request = new Request.Builder().url(url).post(body).build();
        //发送执行请求的客户端
        OkHttpClient client = new OkHttpClient();
        //请求并且返回请求的结果
        Response response = client.newCall(request).execute();
        //返回请求结果
        return response.body().string();
    }


    class PaymentRequest {
        String channel;   //支付渠道
        int amount;   //价格   分

        public PaymentRequest(String channel, int amount) {
            this.channel = channel;
            this.amount = amount;
        }
    }

}
