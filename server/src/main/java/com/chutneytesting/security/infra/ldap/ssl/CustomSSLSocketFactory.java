package com.chutneytesting.security.infra.ldap.ssl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class CustomSSLSocketFactory extends SSLSocketFactory {

    private static SSLSocketFactory theFactory;
    private final SSLSocketFactory defaultFactory;

    /**
     * Returns the default SSL socket factory.
     *
     * <p>The first time this method is called, the security property
     * "ssl.SocketFactory.provider" is examined. If it is non-null, a class by
     * that name is loaded and instantiated. If that is successful and the
     * object is an instance of SSLSocketFactory, it is made the default SSL
     * socket factory.
     *
     * <p>Otherwise, this method returns
     * <code>SSLContext.getDefault().getSocketFactory()</code>. If that
     * call fails, an inoperative factory is returned.
     *
     * @return the default <code>SocketFactory</code>
     * @see SSLContext#getDefault
     */
    public static synchronized SocketFactory getDefault() {
        if (theFactory == null) {
            theFactory = new CustomSSLSocketFactory();
        }
        return theFactory;
    }

    public CustomSSLSocketFactory() {
        defaultFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return defaultFactory.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return defaultFactory.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
        return updateProtocol((SSLSocket) defaultFactory.createSocket(socket, host, port, autoClose));
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        return updateProtocol((SSLSocket) defaultFactory.createSocket(host, port));
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
        return updateProtocol((SSLSocket) defaultFactory.createSocket(host, port, localHost, localPort));
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return updateProtocol((SSLSocket) defaultFactory.createSocket(host, port));
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        return updateProtocol((SSLSocket) defaultFactory.createSocket(address, port, localAddress, localPort));
    }

    private SSLSocket updateProtocol(SSLSocket socket) {
        SSLParameters params = new SSLParameters();
        params.setProtocols(new String[] {"TLSv1.1"});
        socket.setSSLParameters(params);
        return socket;
    }
}
