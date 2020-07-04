package com.soul.base.algorithm.level01;

import java.util.Arrays;

/**
 *
 - 插入排序(ASC):
 第一轮:
 第一个数,与前一个数(null)相比有序不处理( 类似斗地主,每抽一张牌按顺序方式插入原先牌库 )
 第二轮:
 第二个数与第一个数比较, 第二个数较小则交换至第一个位置,否则结束此轮,
 第三轮:
 第三个数与第二个数比较, 第三个数较小则交换至第二个位置,否则结束此轮; 若没结束,则第二个数与第一个数比较, 第二个数较小则交换至第一个位置,否则结束此轮;
 ...
 后续同理(每次循环加入一个数n-m, 与有序区间[1, n-m-1]进行排序合并后, 当前数据[1, n-m]中的最大值, 放置n-m处)
 > 常数操作次数 =  0 * 元素寻址+比较) + 交换 + 1 * (元素寻址+比较) + 交换  ... (n-1) * (元素寻址(查询)+比较) + 交换
 > 常数操作次数 =  0 + 1*2+1 +  2*2+1  +  ...  + (n-1) * (2) + 1
 > 常数操作次数 = ${n^2}$  + n
 > T(n) = O(${n^2}$ )

 特殊情况: {1, 2, 3} 使用插入排序遍历 N-1 次即有序

 > 稳定排序 : (相等数据的顺序保持不变)
 > {7, 7, 3}
 > {7, 3, 7} 原顺序第一个7还是在第二个7前面
 > {3, 7, 7}
 *
 */
public class _03_InsertionSort {

	public static void insertionSort(int[] arr) {
		if (arr == null || arr.length < 2) {
			return;
		}

        for (int i = 1; i < arr.length; i++) { // 下标 1 ~ N-1
            for (int j = i; j > 0; j--) { // 下标为i的数去和 0 ~ i-1
                if (arr[j] < arr[j - 1]){
                    swap(arr, j, j - 1);
                } else {
                    break;
                }
            }
        }



		/*// 0~0 有序的
		// 0~i 想有序
		for (int i = 1; i < arr.length; i++) { // 0 ~ i 做到有序
			for (int j = i - 1; j >= 0 && arr[j] > arr[j + 1]; j--) {
				swap(arr, j, j + 1);
			}
		}*/
	}

	// for getMatchNum
	public static void main(String[] args) {
		int testTime = 500000;
		int maxSize = 100; // 随机数组的长度0～100
		int maxValue = 100;// 值：-100～100
		boolean succeed = true;
		for (int i = 0; i < testTime; i++) {
			int[] arr1 = generateRandomArray(maxSize, maxValue);
			int[] arr2 = copyArray(arr1);
			insertionSort(arr1);
			comparator(arr2);
			if (!isEqual(arr1, arr2)) {
				// 打印arr1
				// 打印arr2
				succeed = false;
				break;
			}
		}
		System.out.println(succeed ? "Nice!" : "OOXX!");

		int[] arr = generateRandomArray(maxSize, maxValue);
        //int[] arr = {3, 2, 1};
		printArray(arr);
		insertionSort(arr);
		printArray(arr);
	}


    // i和j是一个位置的话, 亦或自己则值变成了0
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
        // Math.random() ->  [0,1) 所有的小数，等概率返回一个
        // Math.random() * N -> [0,N) 所有小数，等概率返回一个
        // (int)(Math.random() * N) -> [0,N-1] 所有的整数，等概率返回一个
        int[] arr = new int[(int) ((maxSize + 1) * Math.random())]; // 长度随机
        for (int i = 0; i < arr.length; i++) {
            arr[i] = (int) ((maxValue + 1) * Math.random())
                    - (int) (maxValue * Math.random());
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
