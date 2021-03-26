package com.chutneytesting.tools;

import java.util.concurrent.TimeUnit;

public class WaitUtils {

    public static void awaitDuring(int mills, TimeUnit timeUnit) {
        try {
            timeUnit.sleep(mills);
        } catch (InterruptedException e) {
            throw new RuntimeException("Exception during slepp", e);
        }
    }
}
