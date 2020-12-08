package com.soul.base.algorithm.level01;

import java.util.Arrays;

/**
 *
- 选择排序(ASC):
第一轮:
第一个数与后面每个数比较, 较小值交换至第一个位置;(第一个数字为最小值)
第二轮:
第二个数与后面每个数比较, 较小值交换至第二个位置;(第二个数字为次小值)
...
后续同理(每次循环在[m, n]区间内获取最小值, 放置m处)

> 常数操作次数 = (n-1) * (元素寻址(查询)+比较) + 交换  + (n-2) * (元素寻址+比较) + 交换  ...
> 常数操作次数 = (n-1) * (2) + 1  + (n-2) * (2) + 1  ...  1*2 + 1
> 常数操作次数 = ${n^2}$  + n
> _04_ThreadConcurrent(n) = O(${n^2}$ )

> 不稳定排序 : (相等数据的顺序被打乱)
> {7, 7, 3}
> {3, 7, 7} 原顺序第一个7到了第二个7后面

 *
 */
public class _01_SelectionSort {

    public static void selectionSort(int[] arr) {
        if (arr == null || arr.length < 2) {
            return;
        }
        // 0 ~ N-1
        // 1 ~ N-1
        // ...
        // N-2 ~ N-1
        for (int i = 0; i < arr.length -1; i++){ // i ~ N-2
            for (int j = i + 1; j <= arr.length -1; j++) { // i+1 ~ N-1
                if (arr[j] < arr[i]){
                    swap(arr, i, j);
                }
            }
        }
    }

    public static void selectionSort2(int[] arr) {
        if (arr == null || arr.length < 2) {
            return;
        }
        // 0 ~ N-1
        // 1 ~ N-1
        // ...
        for (int i = 0; i < arr.length - 1; i++) { // 下标 i ~ N-2
            // 记录最小值在哪个位置上(减少交换次数)  i～n-1
            // 常数项时间优于第一种, 细节处理的更好
            int minIndex = i;
            for (int j = i + 1; j < arr.length; j++) { // 下标 i ~ N-1 上找最小值的下标
                minIndex = arr[j] < arr[minIndex] ? j : minIndex;
            }
            swap2(arr, i, minIndex);
        }
    }

    public static void main(String[] args) {
        int testTime = 500000;
        int maxSize = 100;
        int maxValue = 100;
        boolean succeed = true;
        //测试 testTime 次
        for (int i = 0; i < testTime; i++) {
            int[] arr1 = generateRandomArray(maxSize, maxValue);
            int[] arr2 = copyArray(arr1);
            selectionSort(arr1);
            comparator(arr2);
            //不成功则打印
            if (!isEqual(arr1, arr2)) {
                succeed = false;
                printArray(arr1);
                printArray(arr2);
                break;
            }
        }
        System.out.println(succeed ? "Nice!" : "OOXX!");

        int[] arr = generateRandomArray(maxSize, maxValue);
        //int[] arr = {3, 2, 1};
        printArray(arr);
        selectionSort(arr);
        printArray(arr);
    }

    public static void swap(int[] arr, int i, int j) {
        //额外空间 tmp
        int tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
    }

    public static void swap2(int[] arr, int i, int j) {
        //下标一样, 不需要交换, 否则亦或自己则值变成了0
        if (i == j) return;
        //无额外空间
        arr[i] = arr[i] ^ arr[j];
        arr[j] = arr[i] ^ arr[j];
        arr[i] = arr[i] ^ arr[j];
    }

    // jdk 的排序方法
    public static void comparator(int[] arr) {
        Arrays.sort(arr);
    }

    // 生成随机数组
    public static int[] generateRandomArray(int maxSize, int maxValue) {
        // Math.random()   [0,1)
        // Math.random() * N  [0,N)
        // (int)(Math.random() * N)  [0, N-1]
        int[] arr = new int[(int) ((maxSize + 1) * Math.random())];
        for (int i = 0; i < arr.length; i++) {
            // [-? , +?]
            arr[i] = (int) ((maxValue + 1) * Math.random()) - (int) (maxValue * Math.random());
        }
        return arr;
    }

    // 克隆数组
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

    // 自定义排序 与 系统排序 数组每个下标对应的值是否一样
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

    // 打印数组
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
