package com.soul.base.juc.level07;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class T13_ParallelStreamAPI {
	public static void main(String[] args) {
		//单线程处理
		List<Integer> nums = new ArrayList<>();
		Random r = new Random();
		for(int i=0; i<10000; i++) nums.add(1000000 + r.nextInt(1000000));
		//System.out.println(nums);
		
		long start = System.currentTimeMillis();
		nums.forEach(v->isPrime(v));
		long end = System.currentTimeMillis();
		System.out.println("single=" + (end - start));
		
		//使用parallel stream api
		//parallelStream 实现通过 ForkJoinPool
		start = System.currentTimeMillis();
		nums.parallelStream().forEach(T13_ParallelStreamAPI::isPrime);
		end = System.currentTimeMillis();

		System.out.println("parallelStream=" + (end - start));
	}
	
	static boolean isPrime(int num) {
		for(int i=2; i<=num/2; i++) {
			if(num % i == 0) return false;
		}
		return true;
	}
}
