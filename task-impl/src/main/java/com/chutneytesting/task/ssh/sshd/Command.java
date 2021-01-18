package com.chutneytesting.task.ssh.sshd;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.concurrent.Executors;
import org.apache.commons.io.IOUtils;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.SessionAware;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.session.ServerSession;

public class Command implements org.apache.sshd.server.command.Command, SessionAware, Runnable {

    private final SshServerMock sshServerMock;
    private String command = null;

    private InputStream in;
    private OutputStream out;
    private OutputStream err;
    private ExitCallback callback;
    private ServerSession session;

    public Command(SshServerMock sshServerMock, String command) {
        this.sshServerMock = sshServerMock;
        this.command = command;
    }

    public Command(SshServerMock sshServerMock) {
        this.sshServerMock = sshServerMock;
    }

    @Override
    public void setInputStream(InputStream in) {
        this.in = in;
    }

    @Override
    public void setOutputStream(OutputStream out) {
        this.out = out;
    }

    @Override
    public void setErrorStream(OutputStream err) {
        this.err = err;
    }

    @Override
    public void setExitCallback(ExitCallback callback) {
        this.callback = callback;
    }

    @Override
    public void start(ChannelSession channel, Environment env) throws IOException {
        Executors.newSingleThreadExecutor().submit(this);
    }

    @Override
    public void destroy(ChannelSession channel) throws Exception {
    }

    @Override
    public void setSession(ServerSession session) {
        this.session = session;
    }

    @Override
    public void run() {
        try {
            StringBuilder text = new StringBuilder();
            while (true) {
                if (command != null) {
                    text.append(command);
                    addCommandAndExit(text.toString());
                    return;
                } else {
                    if (in.available() > 0) {
                        String t = IOUtils.toString(in, Charset.defaultCharset());
                        text.append(t);
                        continue;
                    }

                    if (text.toString().endsWith("exit")) {
                        addCommandAndExit(text.toString());
                        return;
                    }
                }

                Thread.sleep(1L);
            }
        } catch (Throwable t) {
            uncheckedWriteTo(err, t.getMessage().getBytes(), true);
            callback.onExit(-1, t.getMessage());
        }
    }

    private void addCommandAndExit(String command) {
        Optional<String> stubbedResult = sshServerMock.addCommand(command);
        stubbedResult.map(String::getBytes).ifPresent(b -> uncheckedWriteTo(out, b, false));
        callback.onExit(0);
    }

    private void uncheckedWriteTo(OutputStream out, byte[] bytes, boolean flush) {
        try {
            out.write(bytes);
            if (flush) {
                out.flush();
            }
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }
}
