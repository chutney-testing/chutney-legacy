package com.chutneytesting.task.ssh.sshj;


import com.chutneytesting.task.spi.injectable.Target;
import org.apache.commons.lang3.StringUtils;

public class Connection {

    private static final String EMPTY = "";

    public final String serverHost;
    public final int serverPort;
    public final String username;
    public final String password;
    public final String privateKey;
    public final String passphrase;

    private Connection(String serverHost, int serverPort, String username, String password, String privateKey, String passphrase) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.username = username;
        this.password = password;
        this.privateKey = privateKey;
        this.passphrase = passphrase;
    }

    public static Connection from(Target target) {
        guardClause(target);

        final String host = target.host();
        final int port = extractPort(target);
        final String username = getUsername(target);
        final String password = getPassword(target);
        final String privateKey = getPrivateKey(target);
        final String passphrase = getPassPhrase(target);

        return new Connection(host, port, username, password, privateKey, passphrase);
    }

    public boolean usePrivateKey() {
        return StringUtils.isNotBlank(privateKey);
    }

    private static void guardClause(Target target) {
        if (target.uri() == null) {
            throw new IllegalArgumentException("Target URL is undefined");
        }
        if (target.host() == null || target.host().isEmpty()) {
            throw new IllegalArgumentException("Target is badly defined");
        }
    }

    private static int extractPort(Target target) {
        int serverPort = target.port();
        return serverPort == -1 ? 22 : serverPort;
    }

    private static String getUsername(Target target) {
        if (target.properties().containsKey("username")) {
            return target.properties().get("username");
        }
        if (target.security().credential().isPresent()) {
            return target.security().credential().get().username();
        }
        return EMPTY;
    }

    private static String getPassword(Target target) {
        if (target.properties().containsKey("password")) {
            return target.properties().get("password");
        }
        if (target.security().credential().isPresent()) {
            return target.security().credential().get().password();
        }
        return EMPTY;
    }

    private static String getPrivateKey(Target target) {
        if (target.properties().containsKey("privateKey")) {
            return target.properties().get("privateKey");
        }
        if (target.security().privateKey().isPresent()) {
            return target.security().privateKey().get();
        }
        return EMPTY;
    }

    private static String getPassPhrase(Target target) {
        if (target.properties().containsKey("privateKeyPassphrase")) {
            return target.properties().get("privateKeyPassphrase");
        }
        if (target.security().credential().isPresent()) {
            return target.security().credential().get().password();
        }
        return EMPTY;
    }
}
