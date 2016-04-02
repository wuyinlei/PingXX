package com.yinlei;

public class PaymentRequest {
	String channel; // 支付渠道
	int amount; // 价格 分

	public PaymentRequest(String channel, int amount) {
		this.channel = channel;
		this.amount = amount;
	}
}
