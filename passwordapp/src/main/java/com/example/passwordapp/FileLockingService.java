package com.example.passwordapp;

import java.io.*;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;

public class FileLockingService {

    private static final String ALGORITHM = "AES";  // AES encryption algorithm
    private static final int KEY_LENGTH = 256;  // AES 256-bit encryption
    private static final int ITERATIONS = 10000;  // PBKDF2 iteration count
    private static final int SALT_LENGTH = 16;  // Length of the salt

    // Lock the file by encrypting it
    public void lockFile(File file, String password) throws Exception {
        try {
            byte[] key = getKeyFromPassword(password);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, ALGORITHM));

            // Ensure the file name doesn't conflict with existing ".locked" suffix
            String fileName = file.getName();
            String lockedFileName = fileName.endsWith(".locked") ? fileName : fileName + ".locked";
            File lockedFile = new File(file.getParent(), lockedFileName);

            try (FileInputStream fis = new FileInputStream(file);
                 FileOutputStream fos = new FileOutputStream(lockedFile)) {

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    byte[] encryptedData = cipher.update(buffer, 0, bytesRead);
                    if (encryptedData != null) fos.write(encryptedData);
                }
                byte[] finalData = cipher.doFinal();
                if (finalData != null) fos.write(finalData);

            }
            System.out.println("File locked successfully: " + lockedFile.getPath());
        } catch (IOException e) {
            throw new Exception("Error reading or writing file: " + e.getMessage(), e);
        } catch (InvalidKeyException e) {
            throw new Exception("Invalid encryption key: " + e.getMessage(), e);
        } catch (GeneralSecurityException e) {
            throw new Exception("Encryption error: " + e.getMessage(), e);
        }
    }

    // Unlock the file by decrypting it
    public boolean unlockFile(File file, String password) throws Exception {
        try {
            byte[] key = getKeyFromPassword(password);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, ALGORITHM));

            // Check if the file ends with ".locked" and remove it
            String fileName = file.getName();
            String unlockedFileName = fileName.endsWith(".locked") ? fileName.substring(0, fileName.length() - 7) : fileName;
            File unlockedFile = new File(file.getParent(), unlockedFileName);

            try (FileInputStream fis = new FileInputStream(file);
                 FileOutputStream fos = new FileOutputStream(unlockedFile)) {

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    byte[] decryptedData = cipher.update(buffer, 0, bytesRead);
                    if (decryptedData != null) fos.write(decryptedData);
                }
                byte[] finalData = cipher.doFinal();
                if (finalData != null) fos.write(finalData);

            }
            System.out.println("File unlocked successfully: " + unlockedFile.getPath());
            return true;  // Successfully unlocked
        } catch (IOException e) {
            throw new Exception("Error reading or writing file: " + e.getMessage(), e);
        } catch (InvalidKeyException e) {
            throw new Exception("Invalid decryption key: " + e.getMessage(), e);
        } catch (GeneralSecurityException e) {
            throw new Exception("Decryption failed due to a security error: " + e.getMessage(), e);
        }
    }


    // Generate AES key from password using PBKDF2 (Password-Based Key Derivation Function)
    private byte[] getKeyFromPassword(String password) throws Exception {
        // Generate a random salt
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);

        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        return factory.generateSecret(spec).getEncoded();
    }
}

