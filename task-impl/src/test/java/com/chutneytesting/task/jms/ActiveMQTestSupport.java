package com.chutneytesting.task.jms;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyStore;
import java.util.concurrent.atomic.AtomicLong;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.QueueConnection;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.SslBrokerService;
import org.apache.activemq.broker.SslContext;
import org.apache.activemq.broker.TransportConnector;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.transport.TransportFactory;
import org.apache.activemq.transport.tcp.SslTransportFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;

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

    @BeforeClass
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

    @AfterClass
    public static void tearDown() throws Exception {
        connector.getBrokerService().stop();
    }

    private static TrustManager[] getTrustManager() throws Exception {
        TrustManager[] trustStoreManagers;
        KeyStore trustedCertStore = KeyStore.getInstance("JKS");

        trustedCertStore.load(Resources.getResource("security/truststore.jks").openStream(), "truststore".toCharArray());
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
        ks.load(Resources.getResource("security/server.jks").openStream(), "server".toCharArray());
        kmf.init(ks, "server".toCharArray());
        keystoreManagers = kmf.getKeyManagers();

        return keystoreManagers;
    }

    protected void assertMessageReceived(String queueName, String message) throws IOException, URISyntaxException, JMSException {
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(connector.getConnectUri());
        QueueConnection connection = (QueueConnection) connectionFactory.createConnection();
        connection.start();
        expectedTotalConnections.incrementAndGet(); // One connection done

        QueueSession session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
        Message msg = session.createConsumer(new ActiveMQQueue(queueName)).receive(1000);
        session.close();
        connection.close();

        assertThat(((TextMessage) msg).getText()).isEqualTo(message);
    }

}
