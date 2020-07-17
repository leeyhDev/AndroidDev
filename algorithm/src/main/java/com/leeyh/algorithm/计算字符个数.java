package com.leeyh.algorithm;

import java.util.Scanner;

public class 计算字符个数 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            String s = scanner.nextLine();
            String target =  scanner.next();
            System.out.println(getCharCount(s, target));
        }
    }

    public static int getCharCount(String input, String target) {
        if (input.isEmpty()) return 0;
        char t = target.toLowerCase().charAt(0);
        input = input.toLowerCase();
        int count = 0;
        for (int i = 0; i < input.length(); i++) {
            if (t == input.charAt(i)) {
                count++;
            }
        }
        return count;
    }
}
