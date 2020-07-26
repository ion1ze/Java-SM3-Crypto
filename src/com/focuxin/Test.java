package com.focuxin;

public class Test {
    public static void main(String[] args) {
        String msg = "abc";
        String secretKey = "123";
        System.out.println("消息是："+msg+"\t秘钥是："+secretKey);
        byte[] sm3 = CryptoSM3.hash(msg.getBytes());
        System.out.println("SM3运算结果是:\n"+CryptoSM3.bytesToHexString(sm3));
        byte[] hmacSm3 = CryptoSM3.createHmac(msg.getBytes(),secretKey.getBytes());
        System.out.println("HmacSM3运算结果是:\n"+CryptoSM3.bytesToHexString(hmacSm3));
//        消息是：abc	秘钥是：123
//        SM3运算结果是:
//        66c7f0f462eeedd9d1f2d46bdc10e4e24167c4875cf2f7a2297da02b8f4ba8e0
//        HmacSM3运算结果是:
//        82fb97a1ee8b8153a65d9aabe9a5e86397273029cbabdcb80ff78c623b2e2f3d
    }
}
