package com.chutneytesting.task.jms;

import java.security.KeyStore;
import java.util.concurrent.atomic.AtomicLong;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import org.apache.activemq.broker.SslBrokerService;
import org.apache.activemq.broker.SslContext;
import org.apache.activemq.broker.TransportConnector;
import org.apache.activemq.transport.TransportFactory;
import org.apache.activemq.transport.tcp.SslTransportFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public class ActiveMQTestSupport {

    /**
     * SSL connector One Way
     */
    private static TransportConnector connector;

    /**
     * SSL connector 2 way
     */
    static TransportConnector needClientAuthConnector;

    static final AtomicLong expectedTotalConnections = new AtomicLong();

    @BeforeAll
    public static void setUp() throws Exception {
        // http://java.sun.com/javase/javaseforbusiness/docs/TLSReadme.html
        // work around: javax.net.ssl.SSLHandshakeException: renegotiation is not allowed
        System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "true");

        SslBrokerService service = new SslBrokerService();
        service.setPersistent(false);
        KeyManager[] km = getKeyManager();
        TrustManager[] tm = getTrustManager();
        connector = service.addSslConnector("ssl://localhost:0", km, tm, null);
        //TransportConnector limitedCipherSuites = service.addSslConnector("ssl://localhost:0?transport.enabledCipherSuites=TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256", km, tm, null);
        needClientAuthConnector = service.addSslConnector("ssl://localhost:0?transport.needClientAuth=true", km, tm, null);

        // for client side
        SslTransportFactory sslFactory = new SslTransportFactory();
        SslContext ctx = new SslContext(km, tm, null);
        SslContext.setCurrentSslContext(ctx);
        TransportFactory.registerTransportFactory("ssl", sslFactory);

        service.start();
    }

    @AfterAll
    public static void tearDown() throws Exception {
        connector.getBrokerService().stop();
    }

    private static TrustManager[] getTrustManager() throws Exception {
        TrustManager[] trustStoreManagers;
        KeyStore trustedCertStore = KeyStore.getInstance("JKS");

        trustedCertStore.load(ActiveMQTestSupport.class.getResource("/security/truststore.jks").openStream(), "truststore".toCharArray());
        TrustManagerFactory tmf =
            TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());

        tmf.init(trustedCertStore);
        trustStoreManagers = tmf.getTrustManagers();
        return trustStoreManagers;
    }

    private static KeyManager[] getKeyManager() throws Exception {
        KeyManagerFactory kmf =
            KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        KeyStore ks = KeyStore.getInstance("JKS");
        KeyManager[] keystoreManagers;
        ks.load(ActiveMQTestSupport.class.getResource("/security/server.jks").openStream(), "server".toCharArray());
        kmf.init(ks, "server".toCharArray());
        keystoreManagers = kmf.getKeyManagers();

        return keystoreManagers;
    }

}
