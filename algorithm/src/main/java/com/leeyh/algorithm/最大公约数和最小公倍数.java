package com.leeyh.algorithm;

import java.util.Scanner;

public class 最大公约数和最小公倍数 {

    public static long time;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int a = scanner.nextInt();
        int b = scanner.nextInt();
        System.out.println(minCommonMultiple1(a, b));
    }

    /**
     * 最小公倍数
     */
    public static int minCommonMultiple1(int m, int n) {
        time = System.currentTimeMillis();
        return m * n / maxCommonDivisor(m, n);
    }

    /**
     * 最小公倍数
     */
    public static int minCommonMultiple2(int m, int n) {
        time = System.currentTimeMillis();
        return m * n / subtractionGCD(m, n);
    }

    /**
     * 辗转相除求最大公约数
     * 有两整数a和b：
     * ① a%b得余数c
     * ② 若c=0，则b即为两数的最大公约数
     * ③ 若c≠0，则a=b，b=c，再回去执行①
     */
    public static int maxCommonDivisor(int m, int n) {
        int temp;
        if (m < n) {
            temp = m;
            m = n;
            n = temp;
        }
        while (m % n != 0) {
            temp = m % n;
            m = n;
            n = temp;
        }
        return n;
    }

    /**
     * 相减法求最大公约数
     * 有两整数a和b：
     * ① 若a>b，则a=a-b
     * ② 若a<b，则b=b-a
     * ③ 若a=b，则a（或b）即为两数的最大公约数
     * ④ 若a≠b，则再回去执行①
     */
    public static int subtractionGCD(int m, int n) {
        while (m != n) {
            if (m > n) {
                m = m - n;
            } else {
                n = n - m;
            }
        }
        return m;
    }
}
