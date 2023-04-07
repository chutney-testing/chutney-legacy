package util;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.Semaphore;

public final class SocketUtil {
    private static final Semaphore lock = new Semaphore(1);

    private SocketUtil() {
    }

    public static int freePort() {
        try {
            lock.acquire();
            try (ServerSocket socket = new ServerSocket(0)) {
                socket.setReuseAddress(true);
                return socket.getLocalPort();
            }
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        } finally {
            lock.release();
        }
    }
}
