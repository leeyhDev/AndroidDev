package com.core;

import java.util.HashMap;
import java.util.Scanner;

class PlayGame {
    private static int sStep = 0;
    private static int sLine = 1;
    private static HashMap<Integer, Integer> playNumber = new HashMap<>();

    public static void main(String[] args) {
        playGame();
    }

    private static void init() {
        sStep = 0;
        playNumber.put(1, 3);
        playNumber.put(2, 5);
        playNumber.put(3, 7);
    }

    private static void playGame() {
        init();
        while (!isGameOver()) {
            System.out.println(sStep % 2 == 0 ? "A选手 请输入要修改的行数：" : "B选手 请输入要修改的行数：");
            inputLine();
            inputNumber();
        }
        System.out.println("最后结果：" + playNumber.toString() + (sStep % 2 != 0 ? "，A选手获胜" : "，B选手获胜"));
    }

    private static void inputLine() {
        try {
            Scanner sc = new Scanner(System.in);
            String linestr = sc.nextLine();
            sLine = Integer.parseInt(linestr);
            if (sLine > 3 || sLine < 1) {
                System.out.println("请输入正确的行数：1,2,3");
                inputLine();
            }
        } catch (NumberFormatException e) {
            System.out.println("请输入正确的行数：1,2,3");
        }
    }

    private static void inputNumber() {
        try {
            System.out.println("请输入修改后的数字：");
            Scanner sc = new Scanner(System.in);
            String numberStr = sc.nextLine();
            int number = Integer.parseInt(numberStr);
            if (number >= 0 && number < playNumber.get(sLine)) {
                int temp = playNumber.get(sLine);
                playNumber.put(sLine, number);
                if ((number | playNumber.get(1) | playNumber.get(2) | playNumber.get(3)) == 0) {
                    System.out.println("不可以直接取完");
                    playNumber.put(sLine, temp);
                    inputNumber();
                } else {
                    playNumber.put(sLine, number);
                    sStep++;
                    System.out.println((sStep % 2 != 0 ? "A选手" : "B选手") + "执行步骤" + sStep + "步： " + playNumber.toString());
                }
            } else {
                System.out.println("输入的数字需要比所在行数的数字小且不小于0");
                inputNumber();
            }
        } catch (NumberFormatException e) {
            System.out.println("请输入正确的数字");
        }
    }

    private static boolean isGameOver() {
        return (playNumber.get(1) == 1 && playNumber.get(2) == 0 && playNumber.get(3) == 0) ||
                (playNumber.get(1) == 0 && playNumber.get(2) == 1 && playNumber.get(3) == 0) ||
                (playNumber.get(1) == 0 && playNumber.get(2) == 0 && playNumber.get(3) == 1);
    }
}
