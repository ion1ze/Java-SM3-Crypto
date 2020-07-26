package com.focuxin;

import java.util.Arrays;

public class CryptoSM3 {
    private final byte[] message;
    private byte[] paddedMessage;

    public CryptoSM3(byte[] message) {
        this.message = message;
    }

    private void messagePadding() {
        byte[] message = this.message;
        long keyLength = message.length;//消息字节长度
        final byte[] BIT = {(byte) 0x80};//消息末尾补的1个比特，这里补一个128（1000 0000）
        int outKeyLength = (int) (keyLength % 64);//消息达到64字节所需要的字节
        int zeroLength = (outKeyLength < 55) ? (55 - outKeyLength) : (55 - outKeyLength + 64);//补0的长度，如果长度小于55字节(64字节-8个字节的消息长度的二进制表示-1个字节的比特1)
        byte[] binaryMessageLength = longToBytes(keyLength * 8);//消息比特长度的二进制表示
        byte[] zero = new byte[zeroLength];
        for (int i = 0; i < zeroLength; i++) {
            zero[i] = (byte) 0x00;
        }
        this.paddedMessage = bytesMerge(bytesMerge(bytesMerge(message, BIT), zero), binaryMessageLength);
    }

//    private void iterativeCompression() {
//        byte[] paddedMsg = this.paddedMessage;
//        int[] word = new int[16];
//        int n = (paddedMsg.length) / 64;
//        int[] VI = {0x7380166f, 0x4914b2b9, 0x172442d7, 0xda8a0600, 0xa96f30bc, 0x163138aa, 0xe38dee4d, 0xb0fb0e4e};
//        int[] VX = new int[8];
//        for (int i = 0; i < n; i++) {
//            try {
//                byte[] bytes = Arrays.copyOfRange(paddedMsg, 64 * i, 64 * (i + 1));
//                for (int j = 0; j < 16; j++) {
//                    word[j] = bytesToInt(bytes,j*4);
//                }
//                VI =
//            }
//        }
//    }


    private byte[] longToBytes(long num) {
        byte[] bytes = new byte[8];
        for (int i = 0; i < 8; i++) {
            int offset = (7 - i) * 8;
            bytes[i] = (byte) ((num >>> offset) & 0xff);
        }
        return bytes;
    }

    private int bytesToInt(byte[] bytes, int index) {
        int result = 0;
        result = ((bytes[index] & 0xff) << 24) | ((bytes[index + 1] & 0xff) << 16) | ((bytes[index + 2] & 0xff) << 8) | ((bytes[index + 3] & 0xff));
        return result;
    }

    private byte[] bytesMerge(byte[] front, byte[] rear) {
        byte[] bytes = Arrays.copyOf(front, front.length + rear.length);
        System.arraycopy(rear, 0, bytes, front.length, rear.length);
        return bytes;
    }

    private int[][] extendMessage(int[] message){
        int[][] result = new int[2][];
        int[] w = new int[68];
        int[] w1 = new int[64];
        System.arraycopy(message, 0, w, 0, message.length);
        for(int j=16;j<=67;j++){
            w[j] = P1(w[j-16]^w[j-9]^CircleLeftShift(w[j-3],15)^CircleLeftShift(w[j-13],7)^w[j-6]);
        }
        for (int j=0;j<=63;j++){
            w1[j] = w[j]^w[j+4];
        }
        result[0] = w;
        result[1] = w1;
        return result;
    }

//    private int[] CF(int VI[],int message){
//        int[][] Wx =
//    }

    private int T(int j){
        if(j<=15){
            return 0x79cc4519;
        }else {
            return 0x7a879d8a;
        }
    }

    private int FF(int x,int y ,int z,int j){
        int result = 0;
        if(j>=0&&j<=15){
            result = x^y^z;
        }else if(j>=16&&j<=63){
            result = (x&y)|(x&z)|(y&z);
        }
        return result;
    }

    private int GG(int x,int y,int z,int j){
        int result = 0;
        if(j>=0&&j<=15){
            result = x^y^z;
        }else if(j>=16&&j<=63){
            result = (x&y)|(~x&z);
        }
        return result;
    }

    private static int CircleLeftShift(int x,int n){
        return (n<<n)|(n>>>(32-n));
    }

    private int P0(int x){
        return x^CircleLeftShift(x,9)^CircleLeftShift(x,17);
    }

    private int P1(int x){
        return x^CircleLeftShift(x,15)^CircleLeftShift(x,23);
    }
}
