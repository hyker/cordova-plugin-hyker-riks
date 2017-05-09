package io.hyker.plugin;

import android.app.Activity;

import org.lukhnos.nnio.file.Files;
import org.lukhnos.nnio.file.Paths;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;

import io.hyker.cryptobox.PropertyStore;
import io.hyker.cryptobox.Storage;
import io.hyker.security.Crypto;

/**
 * Created by joakimb on 2017-05-05.
 */

public class AndroidStorage implements Storage {
    private static final String PRIVATE_KEY_EXTENSION = "priv";
    private static final String PUBLIC_KEY_EXTENSION = "pub";
    private static final String SALT_EXTENSION = "salt";

    private final PropertyStore propertyStore;
    private final String workingDir;
    private final KeyStore trustStore;

    public AndroidStorage(PropertyStore propertyStore, Activity activity) throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException {
        this.propertyStore = propertyStore;
        this.workingDir = activity.getApplicationContext().getFilesDir().getAbsolutePath();// + File.separator + "lok";

        String password = propertyStore.TRUST_STORE_PASSWORD;
        try (InputStream inputStream = activity.getAssets().open(propertyStore.TRUST_STORE)) {
            trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(inputStream, password.toCharArray());
        }
    }


    @Override
    public KeyStore loadTrustStore() throws StorageException {
        return trustStore;
    }

    @Override
    public KeyStore loadKeyStore(String uid, String password) throws StorageException{
        String path = null;
        try {
            path = getKeyStorePath(uid, propertyStore);
            try (InputStream inputStream = new FileInputStream(path)) {
                KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                keyStore.load(inputStream, password.toCharArray());
                return keyStore;
            }
        } catch (Exception e) {
            throw new StorageException(e.getMessage());
        }
    }

    @Override
    public KeyStore saveKeyStore(KeyStore keyStore, String uid, String password) throws StorageException{
        String path = null;
        try {
            path = getKeyStorePath(uid, propertyStore);
            File file = new File(path);
            if (file.getParentFile() != null && !file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            file.createNewFile();
            try (OutputStream outputStream = new FileOutputStream(file)) {
                keyStore.store(outputStream, password.toCharArray());
                return keyStore;
            }
        } catch (Exception e) {
            throw new StorageException(e.getMessage());
        }
    }

    @Override
    public KeyPair loadKeys(String uid, String password) throws StorageException{
        try {
            String cryptoKeyPath = getKeyPath(uid, propertyStore);

            byte[] privateEncrypted = load(cryptoKeyPath + PRIVATE_KEY_EXTENSION);
            byte[] publicEncrypted = load(cryptoKeyPath + PUBLIC_KEY_EXTENSION);
            byte[] salt = getSalt(cryptoKeyPath);
            byte[] PBEKey = Crypto.generateKeyFromPassword(salt, password);

            return Crypto.keyPairFromEncryptedBytes(privateEncrypted, publicEncrypted, PBEKey);

        } catch (Exception e) {
            throw new StorageException(e.getMessage());
        }
    }

    @Override
    public void storeKeys(KeyPair keyPair, String uid, String password) throws StorageException{
        try {
            String cryptoKeyPath = getKeyPath(uid, propertyStore);
            createFile(cryptoKeyPath + PRIVATE_KEY_EXTENSION);
            try (FileOutputStream fos = new FileOutputStream(cryptoKeyPath + PRIVATE_KEY_EXTENSION)) {
                fos.write(Crypto.encrypt(Crypto.generateKeyFromPassword(getSalt(cryptoKeyPath), password), keyPair.getPrivate().getEncoded()));
            }

            createFile(cryptoKeyPath + PUBLIC_KEY_EXTENSION);
            try (FileOutputStream fos = new FileOutputStream(cryptoKeyPath + PUBLIC_KEY_EXTENSION)) {
                fos.write(Crypto.encrypt(Crypto.generateKeyFromPassword(getSalt(cryptoKeyPath), password), keyPair.getPublic().getEncoded()));
            }
        } catch (Exception e) {
            throw new StorageException(e.getMessage());
        }
    }

    private String getKeyStorePath(String uid, PropertyStore propertyStore) throws IOException {
        if (propertyStore.CERTIFICATE_EXCHANGE_ENABLED) {
            return workingDir + File.separator + propertyStore.CERTIFICATE_EXCHANGE_KEYSTORE;
        } else if (propertyStore.PUBLIC_KEY_LOOKUP_ENABLED) {
            return workingDir + File.separator + new File(propertyStore.KDI_KEY_STORES_PATH, uid + ".bks").getCanonicalPath();
        } else {
            System.out.println("Neither certificate exchange nor public key lookup is enabled.");
            return null;
        }
    }

    private String getKeyPath(String uid, PropertyStore propertyStore) throws IOException {
        return workingDir + File.separator + new File(propertyStore.KEY_PATH, uid + ".key").getCanonicalPath();
    }

    private byte[] getSalt(String path) throws IOException {
        byte[] salt = new byte[256];
        try (FileInputStream fis = new FileInputStream(path + SALT_EXTENSION)) {
            int read = 0;

            while (read != salt.length) {
                read += fis.read(salt, read, salt.length - read);
            }
        } catch (FileNotFoundException e) {
            new SecureRandom().nextBytes(salt);

            createFile(path + SALT_EXTENSION);
            try (FileOutputStream fos = new FileOutputStream(path + SALT_EXTENSION)) {
                fos.write(salt);
            }
        }
        return salt;
    }

    private static void createFile(String path) throws IOException {
        File file = new File(path);
        if (!file.exists()) {
            if (file.getParentFile() != null && !file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            file.createNewFile();
        }
    }

    private static byte[] load(String path) throws IOException {
        byte[] data;

        try (FileInputStream fis = new FileInputStream(path)) {
            final int length = (int) Files.size(Paths.get(path));
            data = new byte[length];

            int read = 0;
            while (read != data.length) {
                read += fis.read(data, read, data.length - read);
            }
        }

        return data;
    }
}
