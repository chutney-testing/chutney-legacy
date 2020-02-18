package com.chutneytesting.task.ssh;


import com.chutneytesting.task.spi.injectable.Target;
import java.net.URI;

public class Connection {

    private static final String EMPTY = "";

    public final String serverHost;
    public final int serverPort;
    public final String username;
    public final String password;
    public final String privateKey;

    private Connection(String serverHost, int serverPort, String username, String password, String privateKey) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.username = username;
        this.password = password;
        this.privateKey = privateKey;
    }

    static Connection from(Target target) {
        guardClause(target);

        final String host = extractHost(target);
        final int port = extractPort(target);
        final String username = extractUsername(target);
        final String password = extractPassword(target);
        final String privateKey = extractPrivateKey(target);

        return new Connection(host, port, username, password, privateKey);
    }

    private static void guardClause(Target target) {
        if (target.getUrlAsURI() == null) {
            throw new IllegalArgumentException("Target URL is undefined");
        }
        if (target.getUrlAsURI().getHost() == null || target.getUrlAsURI().getHost().isEmpty()) {
            throw new IllegalArgumentException("Target is badly defined");
        }
    }

    private static String extractHost(Target target) {
        return target.getUrlAsURI().getHost();
    }

    private static int extractPort(Target target) {
        URI serverUrl = target.getUrlAsURI();
        return serverUrl.getPort() == -1 ? 22 : serverUrl.getPort();
    }

    private static String extractUsername(Target target) {
        if (target.security().credential().isPresent()) {
            return target.security().credential().get().username();
        }
        return EMPTY;
    }

    private static String extractPassword(Target target) {
        if (target.security().credential().isPresent()) {
            return target.security().credential().get().password();
        }
        return EMPTY;
    }

    private static String extractPrivateKey(Target target) {
        if (target.security().privateKey().isPresent()) {
            return target.security().privateKey().get();
        }
        return EMPTY;
    }
}
