package com.focuxin;

public class Test {
    public static void main(String[] args) {
        String msg = "abc";
        System.out.println(new CryptoSM3(msg.getBytes()).hex());
    }
}
