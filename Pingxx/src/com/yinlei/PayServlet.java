package com.yinlei;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.pingplusplus.Pingpp;
import com.pingplusplus.exception.PingppException;
import com.pingplusplus.model.Charge;
import com.sun.swing.internal.plaf.basic.resources.basic;

import sun.net.www.content.image.gif;

/**
 * Servlet implementation class PayServlet
 */
@WebServlet("/PayServlet")
public class PayServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public PayServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		response.setContentType("application/json;charset=UTF-8");
		//输出
		PrintWriter pw = response.getWriter();
		
		Pingpp.apiKey="sk_test_uLCqfPjTGu9Gm5aHKOLq5qrP";
		
		//获取客户端参数    amount    channel
		ServletInputStream sis = request.getInputStream();
		byte[]bs = new byte[1024];
		int len = -1;
		StringBuffer sb = new StringBuffer();
		while((len = sis.read(bs))!=-1){
			sb.append(new String(bs,0,len));
		}

		Gson gson = new Gson();
		PaymentRequest paymentRequest = gson.fromJson(sb.toString(), PaymentRequest.class);
		
	    Map<String, Object> chargeMap = new HashMap<String, Object>();
	    //某些渠道需要添加extra参数，具体参数详见接口文档
	    chargeMap.put("amount", paymentRequest.amount);   //1元钱
	    chargeMap.put("currency", "cny");  //人民币
	    chargeMap.put("subject", "购买了一款小米5");  //商品的名称
	    chargeMap.put("body", "黑色尊享版");  // 主题内容
	    chargeMap.put("order_no", "12345678902");   //订单编号
	    chargeMap.put("channel", paymentRequest.channel);  //支付渠道
	    chargeMap.put("client_ip", request.getRemoteAddr());  //客户端的IP地址
	    Map<String, String> app = new HashMap<String, String>();
	    app.put("id", "app_nzz1i1rLGWzPmr94");   //应用ID
	    chargeMap.put("app", app);
	    try {
	        //发起交易请求
	        Charge charge = Charge.create(chargeMap);
	        System.out.println(charge);
	        //向客户端输出
	        pw.write(charge.toString());
	    } catch (PingppException e) {
	        e.printStackTrace();
	    }
	}

}
