package com.soul.base.algorithm.level01;

import java.util.Arrays;

/**
 *
 - 从有序数组(ASC)中,查询指定值的元素(直接遍历 O(N); 二分查找 O(${log_2{n}}$):

 根据左下标, 右下标找到数组中间下标位置,
 指定元素与对应位置的值来比较大小, 相等则找到, 小则在左区间找, 大则在右区间找

 *
 */
public class _04_BSExist {

	public static boolean exist(int[] sortedArr, int num) {
		if (sortedArr == null || sortedArr.length == 0) {
			return false;
		}
		int L = 0;
		int R = sortedArr.length - 1;
		int mid = 0;

		// L..R
		while (L < R) {
			// 这种算法存在越界风险
			// mid = (L+R) / 2;
			// L 10亿  R 18亿
			// 避免越界, 加上左下标到右下标的一半距离
			mid = L + ((R - L) >> 1); // mid = (L + R) / 2
			if (sortedArr[mid] == num) {
				return true;
			} else if (sortedArr[mid] > num) {
				R = mid - 1;
			} else {
				L = mid + 1;
			}
		}
		// num = 9 {2, 4, 6}; 最终 L 下标= 1 + 1 = 2; 即取sortedArr[2] 和 9 比较
		// num = 1 {2, 4, 6}; 最终 L 下标= 初始值0; 即取sortedArr[0] 和 1 比较
		return sortedArr[L] == num;
	}

	public static void main(String[] args) {
		int testTime = 500000;
		int maxSize = 100;
		int maxValue = 100;
		boolean succeed = true;
		for (int i = 0; i < testTime; i++) {
			//获取有序数组
			int[] arr = generateRandomArray(maxSize, maxValue);
			Arrays.sort(arr);
			int value = (int) ((maxValue + 1) * Math.random()) - (int) (maxValue * Math.random());
			//查找是否存在随机数value
			if (getMatchNum(arr, value) != exist(arr, value)) {
				succeed = false;
				break;
			}
		}
		System.out.println(succeed ? "Nice!" : "Fucking fucked!");
	}
	
	// O(N)遍历查找
	public static boolean getMatchNum(int[] sortedArr, int num) {
		for(int cur : sortedArr) {
			if(cur == num) {
				return true;
			}
		}
		return false;
	}
	
	
	//随机生成数组
	public static int[] generateRandomArray(int maxSize, int maxValue) {
		int[] arr = new int[(int) ((maxSize + 1) * Math.random())];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = (int) ((maxValue + 1) * Math.random()) - (int) (maxValue * Math.random());
		}
		return arr;
	}
	


}
