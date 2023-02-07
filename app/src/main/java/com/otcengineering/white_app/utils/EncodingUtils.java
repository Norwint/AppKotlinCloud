package com.otcengineering.white_app.utils;

import android.annotation.TargetApi;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import com.otcengineering.white_app.MyApp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;

/**
 * Created by OTC Engineering on 12/7/18 o abans.
 */

public class EncodingUtils {
    static String encode(String input) {
        if (input == null) {
            return null;
        } else if (input.isEmpty()) {
            return "";
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            byte[] encrypted = encrypt(input);
            if (encrypted == null) {
                encrypted = input.getBytes();
            }
            return Base64.encodeToString(encrypted, Base64.DEFAULT);
        } else {
            return Base64.encodeToString(input.getBytes(), Base64.DEFAULT);
        }
    }

    static String decode(String input) {
        if (input.isEmpty()) {
            return "";
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            byte[] decoded = Base64.decode(input, Base64.DEFAULT);
            String decrypted = null;
            try {
                decrypted = decrypt(decoded);
            } catch (IOException e) {
                decrypted = "";
                e.printStackTrace();
            }
            return decrypted.isEmpty() ? "" : decrypted;
        } else {
            byte[] decoded = Base64.decode(input, Base64.DEFAULT);
            return new String(decoded);
        }
    }

    static byte[] encrypt(byte[] input) {
        try {
            SecureRandom secureRandom = new SecureRandom();
            byte[] iv = new byte[12];
            secureRandom.nextBytes(iv);

            final Cipher cipher = Cipher.getInstance("AES/GCM/NOPADDING");
            GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv);

            cipher.init(Cipher.ENCRYPT_MODE, getKey(), parameterSpec);

            byte[] cipherTxt =  cipher.doFinal(input);

            ByteBuffer buffer = ByteBuffer.allocate(iv.length + cipherTxt.length);
            buffer.put(iv);
            buffer.put(cipherTxt);
            Arrays.fill(iv, (byte)0);
            iv = null;
            cipherTxt = null;
            return buffer.array();
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static byte[] encrypt(String input) {
        return encrypt(input.getBytes());
    }

    static byte[] decryptBytes(File file) {
        FileInputStream fis;
        try {
            fis = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        byte[] iv = new byte[12];
        try {
            fis.read(iv);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            final Cipher cipher = Cipher.getInstance("AES/GCM/NOPADDING");
            cipher.init(Cipher.DECRYPT_MODE, getKey(), new GCMParameterSpec(128, iv));
            CipherInputStream cis = new CipherInputStream(fis, cipher);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int readBytes;
            while ((readBytes = cis.read(buf)) >= 0) {
                baos.write(buf, 0, readBytes);
            }
            cis.close();
            fis.close();
            Arrays.fill(iv, (byte)0);
            iv = null;
            return baos.toByteArray();
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException | InvalidKeyException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private synchronized static String decrypt(byte[] input) throws IOException {
        SecureRandom sr = new SecureRandom();
        File file = MyApp.getFile("dec" + System.currentTimeMillis() + sr.nextInt() + ".bin");
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(input);
        fos.close();
        byte[] bs = decryptBytes(file);
        file.delete();
        if (bs == null) {
            return "";
        } else {
            return new String(bs);
        }
    }

    public static byte[] encryptImage(byte[] data) {
        try {
            if (data.length == 0) {
                return null;
            }
            SecureRandom secureRandom = new SecureRandom();
            byte[] iv = new byte[16];
            secureRandom.nextBytes(iv);

            final Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, getKey2(), new IvParameterSpec(iv));

            ByteBuffer buffer = ByteBuffer.allocate(data.length + 16);
            buffer.put(iv);

            // Encriptar en blocks de 10240
            int blockCount = (data.length / 10240) + 1;
            for (int i = 0; i < blockCount; ++i) {
                int sizeBuffer = 10240;
                // Primer, mirem que no sigui l'últim bloc
                if (i == blockCount - 1) {
                    // El búffer serà més petit!
                    sizeBuffer = data.length - (10240 * i);
                }

                // Generem un buffer per emmagatzemar el bloc
                byte[] blockBuffer = new byte[sizeBuffer];

                // Copiem el bloc en el buffer
                System.arraycopy(data, i * 10240, blockBuffer, 0, blockBuffer.length);

                // Encriptem i desem en el buffer extern
                byte[] finalBlock;
                if (sizeBuffer == 10240) {
                    finalBlock = cipher.update(blockBuffer);
                } else {
                    finalBlock = cipher.doFinal(blockBuffer);
                }
                buffer.put(finalBlock);
            }

            return buffer.array();
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] decryptImage(byte[] data) {
        if (data == null || data.length == 0) {
            // 7.8/10, too much nulls
            return null;
        }
        ByteBuffer buffer = ByteBuffer.wrap(data);
        byte[] iv = new byte[16];
        buffer.get(iv);
        byte[] cipherText = new byte[buffer.remaining()];
        buffer.get(cipherText);

        ByteBuffer bb;

        try {
            final Cipher cipher = Cipher.getInstance("AES/CTR/NOPADDING");
            cipher.init(Cipher.DECRYPT_MODE, getKey2(), new IvParameterSpec(iv));
            bb = ByteBuffer.allocate(cipherText.length);

            // Encriptar en blocks de 10240
            int blockCount = (cipherText.length / 10240) + 1;
            for (int i = 0; i < blockCount; ++i) {
                int sizeBuffer = 10240;
                // Primer, mirem que no sigui l'últim bloc
                if (i == blockCount - 1) {
                    // El búffer serà més petit!
                    sizeBuffer = cipherText.length - (10240 * i);
                }

                // Generem un buffer per emmagatzemar el bloc
                byte[] blockBuffer = new byte[sizeBuffer];

                // Copiem el bloc en el buffer
                System.arraycopy(cipherText, i * 10240, blockBuffer, 0, blockBuffer.length);

                // Encriptem i desem en el buffer extern
                byte[] finalBlock;
                if (sizeBuffer == 10240) {
                    finalBlock = cipher.update(blockBuffer);
                } else {
                    finalBlock = cipher.doFinal(blockBuffer);
                }
                bb.put(finalBlock);
            }

            Arrays.fill(iv, (byte)0);
            iv = null;
            return bb.array();
        } catch (NullPointerException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Key getKey() {
        try {
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);
            return ks.getKey("connectechkey", null);
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException | CertificateException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Key getKey2() {
        try {
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);
            return ks.getKey("connectechkey2", null);
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException | CertificateException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static void initEncrypt() {
        try {
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);
            if (!ks.containsAlias("connectechkey")) {
                KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
                keyGenerator.init(new KeyGenParameterSpec.Builder("connectechkey", KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(false)
                .build());
                keyGenerator.generateKey();
            }
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        initEncrypt2();
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static void initEncrypt2() {
        try {
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);
            if (!ks.containsAlias("connectechkey2")) {
                KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
                keyGenerator.init(new KeyGenParameterSpec.Builder("connectechkey2", KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_CTR)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(false)
                .build());
                keyGenerator.generateKey();
            }
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
    }
}
