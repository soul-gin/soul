package com.soul.base.algorithm.level01;

import java.util.Arrays;

/**
 *
- 冒泡排序(ASC):
第一轮:
第一个数与第二个数比较, 较小值交换至第一个位置; 第二个数与第三个数比较, 较小值交换至第二个位置...第n-1与第n个数比较, 较小值交换至第n-1位置;(第n个数字为最大值)
第二轮:
第一个数与第二个数比较, 较小值交换至第一个位置; 第二个数与第三个数比较, 较小值交换至第二个位置...第n-2与第n-1个数比较, 较小值交换至第n-2位置;(第n-1个数字为次最大值)
...
后续同理(每次循环在[1, n-m]区间内获取最大值, 放置n-m处)
> 常数操作次数 = (n-1) * (元素寻址(查询)+比较) + 交换  + (n-2) * (元素寻址+比较) + 交换  ...
> 常数操作次数 = (n-1) * (2) + 1  + (n-2) * (2) + 1  ...  1*2 + 1
> 常数操作次数 = ${n^2}$  + n
> T(n) = O(${n^2}$ )

> 稳定排序 : (相等数据的顺序保持不变)
> {7, 7, 3}
> {7, 3, 7} 原顺序第一个7还是在第二个7前面
> {3, 7, 7}
 *
 */
public class _02_BubbleSort {

	public static void bubbleSort(int[] arr) {
		if (arr == null || arr.length < 2) {
			return;
		}
		// 0 ~ N-1
		// 0 ~ N-2
		// 0 ~ N-3
		//...
		// 0 ~ 1
		for (int i = arr.length - 1; i > 0; i--) { // 下标 0 ~ N - 2 - i
			for (int j = 0; j < i; j++) { // 下标 0 ~ i - 1
				if (arr[j] > arr[j + 1]){
					swap(arr, j, j + 1);
				}
			}
		}
	}

	public static void main(String[] args) {		
		int testTime = 500000;
		int maxSize = 100;
		int maxValue = 100;
		boolean succeed = true;
		for (int i = 0; i < testTime; i++) {
			int[] arr1 = generateRandomArray(maxSize, maxValue);
			int[] arr2 = copyArray(arr1);
			bubbleSort(arr1);
			comparator(arr2);
			if (!isEqual(arr1, arr2)) {
				succeed = false;
				break;
			}
		}
		System.out.println(succeed ? "Nice!" : "OOXX!");

		int[] arr = generateRandomArray(maxSize, maxValue);
		//int[] arr = {3, 2, 1};
		printArray(arr);
		bubbleSort(arr);
		printArray(arr);
	}

	//  i和j是一个位置的话, 亦或自己则值变成了0
	public static void swap(int[] arr, int i, int j) {
		if (i == j) return;
		arr[i] = arr[i] ^ arr[j];
		arr[j] = arr[i] ^ arr[j];
		arr[i] = arr[i] ^ arr[j];
	}

	public static void comparator(int[] arr) {
		Arrays.sort(arr);
	}

	public static int[] generateRandomArray(int maxSize, int maxValue) {
		int[] arr = new int[(int) ((maxSize + 1) * Math.random())];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = (int) ((maxValue + 1) * Math.random()) - (int) (maxValue * Math.random());
		}
		return arr;
	}

	public static int[] copyArray(int[] arr) {
		if (arr == null) {
			return null;
		}
		int[] res = new int[arr.length];
		for (int i = 0; i < arr.length; i++) {
			res[i] = arr[i];
		}
		return res;
	}

	public static boolean isEqual(int[] arr1, int[] arr2) {
		if ((arr1 == null && arr2 != null) || (arr1 != null && arr2 == null)) {
			return false;
		}
		if (arr1 == null && arr2 == null) {
			return true;
		}
		if (arr1.length != arr2.length) {
			return false;
		}
		for (int i = 0; i < arr1.length; i++) {
			if (arr1[i] != arr2[i]) {
				return false;
			}
		}
		return true;
	}

	public static void printArray(int[] arr) {
		if (arr == null) {
			return;
		}
		for (int i = 0; i < arr.length; i++) {
			System.out.print(arr[i] + " ");
		}
		System.out.println();
	}
}
