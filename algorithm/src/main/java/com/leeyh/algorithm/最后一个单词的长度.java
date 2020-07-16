package com.leeyh.algorithm;

import java.util.Scanner;

public class 最后一个单词的长度 {
    public static int lengthOfLastWord(String s) {
        int end = s.length() - 1;
        while (end >= 0 && s.charAt(end) == ' ') {
            end--;
        }
        if (end < 0) return 0;
        int start = end;
        while (start >= 0 && s.charAt(start) != ' ') {
            start--;
        }
        if (start < 0) return end;
        return end - start;
    }

    public static int lengthOfLastWord2(String s) {
        if (s.length() == 0) return 0;
        String[] args = s.split(" ");
        if (args.length == 0) return 0;
        return args[args.length - 1].length();
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            String s = scanner.nextLine();
            System.out.println(lengthOfLastWord2(s));
        }
    }
}
