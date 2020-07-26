# JAVA-SM3-Crypto（国密SM3算法工具类）
做信息安全大作业的时候按照国密SM3公开算法写的工具类，参考了国家密码管理局文档，实现了SM3算法和HmacSM3算法。
## 一、开始使用
SM3:
```java
byte[] sm3 = CryptoSM3.hash(msg.getBytes());
```
HmacSM3:
```java
byte[] hmacSm3 = CryptoSM3.createHmac(msg.getBytes(),secretKey.getBytes());
```
获取16进制字符串结果:
```java
String result = CryptoSM3.bytesToHexString(sm3);
```
## 二、资料
[国家密码管理局关于发布《SM3密码杂凑算法》公告](http://www.oscca.gov.cn/sca/xxgk/2010-12/17/content_1002389.shtml)