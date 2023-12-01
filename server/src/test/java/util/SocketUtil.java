/*
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
