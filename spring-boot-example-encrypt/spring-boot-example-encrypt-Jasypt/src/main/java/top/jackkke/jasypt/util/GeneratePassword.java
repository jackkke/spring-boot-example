package top.jackkke.jasypt.util;

import org.jasypt.util.text.AES256TextEncryptor;
import org.jasypt.util.text.BasicTextEncryptor;
import org.jasypt.util.text.StrongTextEncryptor;

/**
 * @author jackkke
 */
public class GeneratePassword {
    public static void main(String[] args) {
        String pswd = "my_password";
        String text = "hello! I'm from spring-boot-example-encrypt-jasypt";
        // UnSBYAMao6BPWZKFwl6zGHgGL0VLX14tMsAgOsjz3tkrIUzyUIIOUxAwHAvTmdyA
        System.out.println(aes256Algorithm(pswd, text));
        // /BNrDGRpCgHLgMfhCpLnuA==
        System.out.println(basicAlgorithm(pswd, text));
        // GHeIkMHO/y04MLNxTDg+sg==
        System.out.println(StrongAlgorithm(pswd, text));
    }

    // 默认算法
    // 对应配置(默认，可省略) jasypt.encryptor.algorithm=PBEWithHMACSHA512AndAES_256
    private static String aes256Algorithm(String pswd, String text) {
        AES256TextEncryptor textEncryptor = new AES256TextEncryptor();
        textEncryptor.setPassword(pswd);
        return textEncryptor.encrypt(text);
    }

    // 基础算法
    // 对应配置 jasypt.encryptor.algorithm=PBEWithMD5AndDES
    private static String basicAlgorithm(String pswd, String text) {
        BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
        textEncryptor.setPassword(pswd);
        return textEncryptor.encrypt(text);
    }

    // 增强算法
    // 对应配置 jasypt.encryptor.algorithm=PBEWithMD5AndTripleDES
    private static String StrongAlgorithm(String pswd, String text) {
        StrongTextEncryptor textEncryptor = new StrongTextEncryptor();
        textEncryptor.setPassword(pswd);
        return textEncryptor.encrypt(text);
    }
}
