package com.chutneytesting.tools;

@FunctionalInterface
public interface ThrowingRunnable extends ThrowingSupplier<Void, Exception> {
    void run() throws Exception;

    @Override
    default Void get() throws Exception {
        run();
        return null;
    }
}
