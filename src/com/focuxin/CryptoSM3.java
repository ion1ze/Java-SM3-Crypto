package com.focuxin;

import java.util.Arrays;

public class CryptoSM3 {

    /**
     * SM3算法
     *
     * @param message 消息
     * @return 结果
     */
    public static byte[] hash(byte[] message) {
        byte[] paddedMsg = messagePadding(message); //消息填充
        return iterativeCompression(paddedMsg); //迭代压缩
    }

    /**
     * HmacSM3算法
     *
     * @param message 消息
     * @param key     秘钥
     * @return 结果
     */
    public static byte[] createHmac(byte[] message, byte[] key) {
        final int LENGTH = 64;
        byte[] actualKey;
        int zeroLength = LENGTH - key.length;
        byte[] zero = new byte[zeroLength];
        for (int i = 0; i < zeroLength; i++) {
            zero[i] = (byte) 0x00;
        }
        if (key.length > 64) {
            actualKey = CryptoSM3.hash(key);
        } else {
            actualKey = bytesMerge(key, zero);
        }
        byte[] iPadXOR = new byte[LENGTH];
        byte[] oPadXOR = new byte[LENGTH];
        for (int i = 0; i < LENGTH; i++) {
            iPadXOR[i] = (byte) (actualKey[i] ^ 0x36);
            oPadXOR[i] = (byte) (actualKey[i] ^ 0x5c);
        }
        byte[] firstMerge = bytesMerge(iPadXOR, message);
        byte[] firstHash = CryptoSM3.hash(firstMerge);
        byte[] secondMerge = bytesMerge(oPadXOR, firstHash);
        return CryptoSM3.hash(secondMerge);
    }

    /**
     * 消息填充
     * 把消息填充到512比特倍数的消息
     *
     * @param msg 消息
     * @return 512比特倍数的消息
     */
    private static byte[] messagePadding(byte[] msg) {
        long keyLength = msg.length;
        final byte[] BIT = {(byte) 0x80};
        int outKeyLength = (int) (keyLength % 64);
        int zeroLength = (outKeyLength < 55) ? (55 - outKeyLength) : (55 - outKeyLength + 64);
        byte[] binaryMessageLength = longToBytes(keyLength * 8);
        byte[] zero = new byte[zeroLength];
        for (int i = 0; i < zeroLength; i++) {
            zero[i] = (byte) 0x00;
        }
        return bytesMerge(bytesMerge(bytesMerge(msg, BIT), zero), binaryMessageLength);
    }

    /**
     * 消息扩展
     * 消息分组扩展成132个字
     *
     * @param word 字消息分组
     * @return 扩展后的字分组
     */
    private static int[][] extendMessage(int[] word) {
        int[][] result = new int[2][];
        int[] w = new int[68];
        int[] w1 = new int[64];
        for (int i = 0; i < word.length; i++) {
            w[i] = word[i];
        }
        for (int j = 16; j <= 67; j++) {
            w[j] = P1(w[j - 16] ^ w[j - 9] ^ CircleLeftShift(w[j - 3], 15)) ^ CircleLeftShift(w[j - 13], 7) ^ w[j - 6];
        }
        for (int j = 0; j <= 63; j++) {
            w1[j] = w[j] ^ w[j + 4];
        }
        result[0] = w;
        result[1] = w1;
        return result;
    }

    /**
     * 迭代压缩
     * 把填充后的消息按512比特分组,然后按照文档给的方式压缩迭代
     *
     * @param paddedMsg 填充后的消息
     * @return 摘要结果
     */
    private static byte[] iterativeCompression(byte[] paddedMsg) {
        int[] word = new int[16];
        int n = (paddedMsg.length) / 64;
        int[] VI = {0x7380166f, 0x4914b2b9, 0x172442d7, 0xda8a0600, 0xa96f30bc, 0x163138aa, 0xe38dee4d, 0xb0fb0e4e};
        int[] VX = new int[8];
        for (int i = 0; i < n; i++) {
            try {
                byte[] bytes = Arrays.copyOfRange(paddedMsg, 64 * i, 64 * (i + 1));
                for (int j = 0; j < 16; j++) {
                    word[j] = bytesToWord(bytes, j * 4);
                }
                VI = CF(VI, word);
                VX = VI;
            } catch (Exception e) {
                e.getStackTrace();
            }
        }
        return wordsToBytes(VX);
    }

    /**
     * long型转byte数组
     *
     * @param num 数
     * @return byte数组
     */
    private static byte[] longToBytes(long num) {
        byte[] bytes = new byte[8];
        for (int i = 0; i < 8; i++) {
            int offset = (7 - i) * 8;
            bytes[i] = (byte) ((num >>> offset) & 0xff);
        }
        return bytes;
    }

    /**
     * 字转byte数组
     *
     * @param words 字（长度为32的比特串）
     * @return byte数组
     */
    private static byte[] wordsToBytes(int[] words) {
        byte[] bytes = new byte[32];
        for (int i = 0; i < words.length; i++) {
            for (int j = 0; j < 4; j++) {
                int offset = (3 - j) * 8;
                bytes[j + (i * 4)] = (byte) ((words[i] >>> offset) & 0xff);
            }
        }
        return bytes;
    }

    /**
     * byte数组转字
     *
     * @param bytes byte数组
     * @param index 数组下标
     * @return 字
     */
    private static int bytesToWord(byte[] bytes, int index) {
        int result;
        result = ((bytes[index] & 0xff) << 24) | ((bytes[index + 1] & 0xff) << 16) | ((bytes[index + 2] & 0xff) << 8) | ((bytes[index + 3] & 0xff));
        return result;
    }

    /**
     * byte数组转16进制字符串
     *
     * @param bytes bytes数组
     * @return 16进制字符串
     */
    public static String bytesToHexString(byte[] bytes) {
        char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        StringBuffer buffer = new StringBuffer();
        for (byte b : bytes) {
            char c = hexDigits[(b >>> 4) & 0xf];
            buffer.append(c);
            c = hexDigits[b & 0xf];
            buffer.append(c);
        }
        return buffer.toString();
    }

    /**
     * 数组拼接
     *
     * @param front 拼接在前面的数组
     * @param rear  拼接在后面的数组
     * @return 拼接后的数组
     */
    private static byte[] bytesMerge(byte[] front, byte[] rear) {
        byte[] bytes = Arrays.copyOf(front, front.length + rear.length);
        System.arraycopy(rear, 0, bytes, front.length, rear.length);
        return bytes;
    }

    /**
     * 压缩函数
     *
     * @param VI   记录压缩函数寄存器的初态
     * @param word 中间值
     * @return 压缩后结果
     */
    private static int[] CF(int[] VI, int[] word) {
        int[][] wx = extendMessage(word);
        int[] w = wx[0];
        int[] w1 = wx[1];
        int[] result = new int[8];
        int A, B, C, D, E, F, G, H, SS1, SS2, TT1, TT2;
        A = VI[0];
        B = VI[1];
        C = VI[2];
        D = VI[3];
        E = VI[4];
        F = VI[5];
        G = VI[6];
        H = VI[7];
        for (int j = 0; j <= 63; j++) {
            SS1 = CircleLeftShift(CircleLeftShift(A, 12) + E + CircleLeftShift(T(j), j), 7);
            SS2 = SS1 ^ CircleLeftShift(A, 12);
            TT1 = FF(A, B, C, j) + D + SS2 + w1[j];
            TT2 = GG(E, F, G, j) + H + SS1 + w[j];
            D = C;
            C = CircleLeftShift(B, 9);
            B = A;
            A = TT1;
            H = G;
            G = CircleLeftShift(F, 19);
            F = E;
            E = P0(TT2);
        }
        result[0] = A ^ VI[0];
        result[1] = B ^ VI[1];
        result[2] = C ^ VI[2];
        result[3] = D ^ VI[3];
        result[4] = E ^ VI[4];
        result[5] = F ^ VI[5];
        result[6] = G ^ VI[6];
        result[7] = H ^ VI[7];
        return result;
    }

    /**
     * 常量，随j变化而变化
     *
     * @param j 组数
     * @return 结果
     */
    private static int T(int j) {
        if (j <= 15) {
            return 0x79cc4519;
        } else {
            return 0x7a879d8a;
        }
    }

    /**
     * 布尔函数
     *
     * @param x 待运算数
     * @param y 待运算数
     * @param z 待运算数
     * @param j 组数
     * @return 运算结果
     */
    private static int FF(int x, int y, int z, int j) {
        int result = 0;
        if (j >= 0 && j <= 15) {
            result = x ^ y ^ z;
        } else if (j >= 16 && j <= 63) {
            result = (x & y) | (x & z) | (y & z);
        }
        return result;
    }

    /**
     * 布尔函数
     *
     * @param x 待运算数
     * @param y 待运算数
     * @param z 待运算数
     * @param j 待运算数
     * @return 运算结果
     */
    private static int GG(int x, int y, int z, int j) {
        int result = 0;
        if (j >= 0 && j <= 15) {
            result = x ^ y ^ z;
        } else if (j >= 16 && j <= 63) {
            result = (x & y) | (~x & z);
        }
        return result;
    }

    /**
     * 旋转左移位运算
     *
     * @param x 待运算数
     * @param n 变化量
     * @return 旋转左移位运算结果
     */
    private static int CircleLeftShift(int x, int n) {
        return (x << n) | (x >>> (32 - n));
    }

    /**
     * 压缩函数中的置换函数
     *
     * @param x 被置换数
     * @return 置换结果
     */
    private static int P0(int x) {
        return x ^ CircleLeftShift(x, 9) ^ CircleLeftShift(x, 17);
    }

    /**
     * 消息扩展中的置换函数
     *
     * @param x 被置换数
     * @return 置换结果
     */
    private static int P1(int x) {
        return x ^ CircleLeftShift(x, 15) ^ CircleLeftShift(x, 23);
    }
}
