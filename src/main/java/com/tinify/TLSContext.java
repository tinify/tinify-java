package com.tinify;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Collection;

public class TLSContext {
    public static SSLSocketFactory socketFactory;
    public static X509TrustManager trustManager;

    static {
        try {
            KeyStore keyStore = certificateKeyStore();

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(
                    KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, null);

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);

            KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();

            if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                throw new AssertionError("Unexpected default trust managers.");
            }

            javax.net.ssl.SSLContext sslContext = javax.net.ssl.SSLContext.getInstance("TLS");
            sslContext.init(keyManagers, trustManagers, null);

            socketFactory = sslContext.getSocketFactory();
            trustManager = (X509TrustManager) trustManagers[0];

        } catch (GeneralSecurityException err) {
            throw new AssertionError("Unexpected error while configuring TLS. No TLS available?", err);
        }
    }

    private static KeyStore certificateKeyStore() throws GeneralSecurityException {
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());

            /* By convention, a null InputStream creates an empty key store. */
            keyStore.load(null, null);

            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            Collection<? extends Certificate> certificates = certificateFactory.generateCertificates(certificateStream());

            int index = 0;
            for (Certificate certificate : certificates) {
                String certificateAlias = Integer.toString(index++);
                keyStore.setCertificateEntry(certificateAlias, certificate);
            }

            if (keyStore.size() == 0) {
                /* The resource stream was empty, no certificates were found. */
                throw new AssertionError("Unable to load any CA certificates.");
            }

            return keyStore;
        } catch (IOException err) {
            throw new AssertionError(err);
        }
    }

    private static InputStream certificateStream() throws IOException {
        return TLSContext.class.getResourceAsStream("/cacert.pem");
    }
}
