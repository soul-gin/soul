package com.soul.base.juc.level05;

import java.util.concurrent.LinkedTransferQueue;

public class T29_TransferQueue {

	//传递队列
	//使用场景: 支付操作必须等待支付结果
	//类似MQ的确保消息投递成功
	public static void main(String[] args) throws InterruptedException {
		LinkedTransferQueue<String> strs = new LinkedTransferQueue<>();

		new Thread(() -> {
			try {
				//等待2秒
				Thread.sleep(2000);
				System.out.println("处理完成:" + strs.take());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}).start();

		System.out.println("begin......");
		//必须等待 transfer 的数据被处理完成才会结束等待
		strs.transfer("aaa");
		System.out.println("end......");
	}

	//要求用线程顺序打印A1B2C3....Z26


}
