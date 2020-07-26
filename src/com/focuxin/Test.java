package com.focuxin;

public class Test {
    public static void main(String[] args) {
        String msg = "abc";
        System.out.println(CryptoSM3.bytesToHexString(CryptoSM3.hash(msg.getBytes())));
    }
}
