package com.soul.base.juc.level07;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;

/**
 * ThreadPoolExecutor : 线程共享同一个队列
 * ForkJoinPool: 每个线程有自己的任务队列
 *
 * ForkJoinPool
 * 分解汇总的任务(类似 MapReduce 处理逻辑)
 * 用很少的线程可以执行很多的任务(子任务)ThreadPoolExecutor做不到先执行子任务
 * CPU密集型
 *
 */
public class T12_ForkJoinPool {
	static int[] nums = new int[1000000];
	//RecursiveAction是一种ForkJoinPool的实现, MAX_NUM是自定义的任务最小分片
	static final int MAX_NUM = 50000;
	static Random r = new Random();
	
	static {
		for(int i=0; i<nums.length; i++) {
			//数组中每个位置数随机生成
			nums[i] = r.nextInt(100);
		}

		//stream api 对数组中随机生成的数进行求和计算
		System.out.println("------ begin result:" + Arrays.stream(nums).sum() + " ------");
	}
	
	//RecursiveAction 是没有返回值的任务
	static class AddTask extends RecursiveAction {

		//起始位置, 结束位置
		int start, end;

		AddTask(int s, int e) {
			start = s;
			end = e;
		}

		@Override
		protected void compute() {

			//根据最大分片大小判断是否还需要拆分任务
			if(end-start <= MAX_NUM) {
				//在 MAX_NUM 以内, 直接计算
				long sum = 0L;
				for(int i=start; i<end; i++) sum += nums[i];
				System.out.println("from:" + start + " to:" + end + " = " + sum);
			} else {
				//超过 MAX_NUM 进行任务再拆分
				int middle = start + (end-start)/2;

				//任务分片
				AddTask subTask1 = new AddTask(start, middle);
				AddTask subTask2 = new AddTask(middle, end);
				subTask1.fork();
				subTask2.fork();
			}


		}

	}

	//RecursiveTask 有返回值的任务
	static class AddTaskRet extends RecursiveTask<Long> {
		
		private static final long serialVersionUID = 1L;
		int start, end;
		
		AddTaskRet(int s, int e) {
			start = s;
			end = e;
		}

		@Override
		protected Long compute() {
			//类似递归处理

			//小任务直接计算
			if(end-start <= MAX_NUM) {
				long sum = 0L;
				for(int i=start; i<end; i++) sum += nums[i];
				return sum;
			} else {
				//大任务进行拆分
				int middle = start + (end-start)/2;
				AddTaskRet subTask1 = new AddTaskRet(start, middle);
				AddTaskRet subTask2 = new AddTaskRet(middle, end);
				subTask1.fork();
				subTask2.fork();

				//汇总任务的结果
				return subTask1.join() + subTask2.join();
			}

		}
		
	}
	
	public static void main(String[] args) throws IOException {
		//RecursiveAction 无返回值任务测试
		/*ForkJoinPool fjp = new ForkJoinPool();
		AddTask task = new AddTask(0, nums.length);
		fjp.execute(task);*/

		System.out.println("------------------------------------");

		ForkJoinPool fjp2 = new ForkJoinPool();
		AddTaskRet task2 = new AddTaskRet(0, nums.length);
		fjp2.execute(task2);
		long result = task2.join();
		System.out.println("------ join result=" + result + " ------");
		
		//System.in.read();
		
	}
}
