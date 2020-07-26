package com.focuxin;

import java.util.Arrays;

public class CryptoSM3 {
    private final byte[] message;
    private byte[] paddedMessage;
    private byte[] sm3Result;

    public CryptoSM3(byte[] message) {
        this.message = message;
        messagePadding();
        iterativeCompression();
    }

    public String hex() {
        return bytesToHex(sm3Result);
    }

    private void messagePadding() {
        byte[] msg = message;
        long keyLength = msg.length;//消息字节长度
        final byte[] BIT = {(byte) 0x80};//消息末尾补的1个比特，这里补一个128（1000 0000）
        int outKeyLength = (int) (keyLength % 64);//消息达到64字节所需要的字节
        int zeroLength = (outKeyLength < 55) ? (55 - outKeyLength) : (55 - outKeyLength + 64);//补0的长度，如果长度小于55字节(64字节-8个字节的消息长度的二进制表示-1个字节的比特1)
        byte[] binaryMessageLength = longToBytes(keyLength * 8);//消息比特长度的二进制表示
        byte[] zero = new byte[zeroLength];//填充0
        for (int i = 0; i < zeroLength; i++) {
            zero[i] = (byte) 0x00;
        }
        paddedMessage = bytesMerge(bytesMerge(bytesMerge(msg, BIT), zero), binaryMessageLength);
    }

    private int[][] extendMessage(int[] msg) {
        int[][] result = new int[2][];
        int[] w = new int[68];
        int[] w1 = new int[64];
        for (int i = 0; i < msg.length; i++) {
            w[i] = msg[i];
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

    private void iterativeCompression() {
        byte[] paddedMsg = paddedMessage;
        int[] word = new int[16];
        int n = (paddedMsg.length) / 64;
        int[] VI = {0x7380166f, 0x4914b2b9, 0x172442d7, 0xda8a0600, 0xa96f30bc, 0x163138aa, 0xe38dee4d, 0xb0fb0e4e};
        int[] VX = new int[8];
        for (int i = 0; i < n; i++) {
            try {
                byte[] bytes = Arrays.copyOfRange(paddedMsg, 64 * i, 64 * (i + 1));
                for (int j = 0; j < 16; j++) {
                    word[j] = bytesToInt(bytes, j * 4);
                }
                VI = CF(VI, word);
                VX = VI;
            } catch (Exception e) {
                e.getStackTrace();
            }
        }
        sm3Result = wordsToBytes(VX);
    }


    private byte[] longToBytes(long num) {
        byte[] bytes = new byte[8];
        for (int i = 0; i < 8; i++) {
            int offset = (7 - i) * 8;
            bytes[i] = (byte) ((num >>> offset) & 0xff);
        }
        return bytes;
    }

    private byte[] wordsToBytes(int[] num) {
        byte[] bytes = new byte[32];
        for (int i = 0; i < num.length; i++) {
            for (int j = 0; j < 4; j++) {
                int offset = (3 - j) * 8;
                bytes[j + (i * 4)] = (byte) ((num[i] >>> offset) & 0xff);
            }
        }
        return bytes;
    }

    private int bytesToInt(byte[] bytes, int index) {
        int result;
        result = ((bytes[index] & 0xff) << 24) | ((bytes[index + 1] & 0xff) << 16) | ((bytes[index + 2] & 0xff) << 8) | ((bytes[index + 3] & 0xff));
        return result;
    }

    private String bytesToHex(byte[] bytes) {
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

    private byte[] bytesMerge(byte[] front, byte[] rear) {
        byte[] bytes = Arrays.copyOf(front, front.length + rear.length);
        System.arraycopy(rear, 0, bytes, front.length, rear.length);
        return bytes;
    }

    private int[] CF(int[] VI, int[] message) {
        int[][] wx = extendMessage(message);
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

    private int T(int j) {
        if (j <= 15) {
            return 0x79cc4519;
        } else {
            return 0x7a879d8a;
        }
    }

    private int FF(int x, int y, int z, int j) {
        int result = 0;
        if (j >= 0 && j <= 15) {
            result = x ^ y ^ z;
        } else if (j >= 16 && j <= 63) {
            result = (x & y) | (x & z) | (y & z);
        }
        return result;
    }

    private int GG(int x, int y, int z, int j) {
        int result = 0;
        if (j >= 0 && j <= 15) {
            result = x ^ y ^ z;
        } else if (j >= 16 && j <= 63) {
            result = (x & y) | (~x & z);
        }
        return result;
    }

    private static int CircleLeftShift(int x, int n) {
        return (x << n) | (x >>> (32 - n));
    }

    private int P0(int x) {
        return x ^ CircleLeftShift(x, 9) ^ CircleLeftShift(x, 17);
    }

    private int P1(int x) {
        return x ^ CircleLeftShift(x, 15) ^ CircleLeftShift(x, 23);
    }
}
