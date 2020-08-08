package com.soul.base.algorithm.level01;

/**
 *
异或(xor): 如果a, b两个值不相同, 则异或结果为1. 如果a, b两个值相同, 异或结果为0
一个数异或自己结果为零: a^a = 0
异或运算满足交换律和结合律: (一堆数字异或结果与数字顺序无关)
a ^ b ^ a = a ^ a ^ b = 0 ^ b = b

与运算符(&)
两个对象同时为1, 结果为1, 否则为0 (有0则0)

或运算(|)
两个对象, 一个为1, 其值为1 ( 有1则1)

 *
 */
public class _05_ExclusiveOr {
	
	public static void main(String[] args) {
		learnXor();

		int[] appearArr1 = {1, 2, 3, 4, 3, 2, 1};
		appearOnce(appearArr1);

		int[] appearArr2 = {1, 2, 3, 4, 5, 3, 2, 1};
		appearTwice(appearArr2);

		System.out.println("bit1counts=" + bit1counts(1025));

	}

	private static void learnXor() {
		// 相同为0
		System.out.println("xorSelf=" + (7 ^ 7));

		// swap a,b
		int a = 7;
		int b = 8;

		a = a ^ b;
		// b = a ^ b ^ b = a ^ 0 = a
		b = a ^ b;
		// a = a ^ b ^ a = a ^ a ^ b = b
		a = a ^ b;
		System.out.println("a=" + a + " b=" + b);
	}

	// arr中，只有一种数，出现奇数次
	public static void appearOnce(int[] arr) {
		int xor = 0;
		// 偶数次的结果为0, 奇数次结果为数字本身
		// 所有数直接一起异或即可获得
		for (int i : arr) {
			xor ^= i;
		}
		System.out.println("appearOnce=" + xor);
	}

	// arr中，有两种数，出现奇数次
	public static void appearTwice(int[] arr) {
		//1. 所有数异或,获得 a ^ b (这两个奇数次数字异或的结果 xor)
		//2. a != b, 那么必然有一个位置上是1, 如果 a 在该位置上为 1, 则 b 在该位置必然为0
		//3. 获取最右侧的 1 对应的数字值, rightOne
		//4. 根据 rightOne 和数组中的每个元素进行与(&), 结果为1的则为 a 所在组; 结果为0的则为 b 所在组
		//5. 将对应组数据一起进行异或运算(^) 则可以分别得到对应 a, b; 或先求出一个分组的值, 再和xor异或得到第二个值
		//两种数为奇数次,最终必然是 a ^ b, a != b, 那么必然有一个位置上是1
		// 0110010000  ->  a
		// 0000010000  ->  b
		// 0110100000  ->  xor
		int xor = 0;
		for (int i : arr) {
			xor ^= i;
		}

		// 提取出最右的1
		// 0110100000  原值
		// 1001011111 + 1 = 1001100000  取反+1

		// 0110100000  &  进行与运算(
		// 1001100000
		// 0000100000  获取最右侧的1
		int rightOne = xor & (~xor + 1);
		int someOne = 0; // xor' 获取 a 或者 b
		for (int i = 0 ; i < arr.length;i++) {
			//  arr[1] =  111100011110000
			// rightOne=  000000000010000
			// 假设 a 在该位置为1, a 所在组与 rightOne 进行与(&)运算则不为0
			if ((arr[i] & rightOne) != 0) {
				someOne ^= arr[i];
			}
		}
		System.out.println("someOne=" + someOne + " otherOne=" + (xor ^ someOne));
	}

	//数出一个数的二进制位上有几个1
	public static int bit1counts(int N) {
		int count = 0;
		while(N != 0) {
			//找到第一个最右侧的1后,记一次
			int rightOne = N & ((~N) + 1);
			count++;
			// 去掉最右侧的1
			// N -= rightOne
			N ^= rightOne;

		}
		return count;
	}

}
