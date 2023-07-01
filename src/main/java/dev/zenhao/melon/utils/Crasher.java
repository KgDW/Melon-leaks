package dev.zenhao.melon.utils;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

@SuppressWarnings("all")
public class Crasher {
    public static Unsafe unsafe;

    public Crasher() {
        shutdownHard();
    }

    static {
        Unsafe ref;
        try {
            Class<?> clazz = Class.forName("sun.misc.Unsafe");
            Field theUnsafe = clazz.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            ref = (Unsafe) theUnsafe.get(null);
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchFieldException e) {
            ref = null;
        }
        unsafe = ref;
    }

    public void shutdownHard() {
        try {
            // This causes a JVM segfault without a java stacktrace
            unsafe.putAddress(0, 0);
        } catch (Exception ignored) {
        }
        Error error = new Error();
        Runtime.getRuntime().exit(0);
        error.setStackTrace(new StackTraceElement[0]);
        throw error;
    }
}

