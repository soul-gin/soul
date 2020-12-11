
package com.soul.base.juc.level02;

import java.util.concurrent.TimeUnit;

/**
 * volatile �ؼ��֣�ʹһ�������ڶ���̼߳�ɼ�
 * A B�̶߳��õ�һ��������javaĬ����A�߳��б���һ��copy���������B�߳��޸��˸ñ�������A�߳�δ��֪��
 * ʹ��volatile�ؼ��֣����������̶߳�������������޸�ֵ
 *
 * ������Ĵ����У�running�Ǵ����ڶ��ڴ��t������
 * ���߳�t1��ʼ���е�ʱ�򣬻��runningֵ���ڴ��ж���t1�̵߳Ĺ������������й�����ֱ��ʹ�����copy��������ÿ�ζ�ȥ
 * ��ȡ���ڴ棬�����������߳��޸�running��ֵ֮��t1�̸߳�֪���������Բ���ֹͣ����
 *
 * ʹ��volatile������ǿ�������̶߳�ȥ���ڴ��ж�ȡrunning��ֵ
 *
 * �����Ķ���ƪ���½��и����������
 * http://www.cnblogs.com/nexiyi/p/java_memory_model_and_thread.html
 *
 * volatile�����ܱ�֤����̹߳�ͬ�޸�running����ʱ�������Ĳ�һ�����⣬Ҳ����˵volatile�������synchronized
 */
public class T01_HelloVolatile {
	//�Ա�һ������volatile������£������������н��������
	volatile boolean runAndEnd = true;
	/*volatile*/ boolean running = true;
	void m1() {
		System.out.println("m1 start");
		while(runAndEnd) {

		}
		System.out.println("m1 end!");
	}

	void m2() {
		System.out.println("m2 start");
		while(running) {

		}
		System.out.println("m2 end!");
	}
	
	public static void main(String[] args) {
		T01_HelloVolatile t = new T01_HelloVolatile();

		new Thread(t::m1, "t1").start();

		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		//���� volatile, ���̵߳��޸�, �����߳̿ɼ�
		//���Կ���ֹͣ
		t.runAndEnd = false;

		new Thread(t::m2, "t1").start();

		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		//û�� volatile, ���̵߳��޸�, �����̲߳��ɼ�
		//һֱ��ѭ��
		t.runAndEnd = false;
	}
	
}

