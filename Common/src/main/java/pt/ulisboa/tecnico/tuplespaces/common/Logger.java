package pt.ulisboa.tecnico.tuplespaces.common;

import java.util.concurrent.atomic.AtomicBoolean;

// Helper class to print debug messages.
public final class Logger {

    private static final AtomicBoolean debugFlag =
            new AtomicBoolean(System.getProperty("debug") != null);

    private Logger() {
    }


    public static void setDebugFlag(boolean flag) {
        debugFlag.set(flag);
    }


    public static void debug(String debugMessage, Object... args) {
        if (debugFlag.get()) {
            String formattedMessage = String.format(debugMessage, args);
            System.out.println(formattedMessage);
        }
    }

    public static void error(String errorMessage, Object... args) {
        String formattedMessage = String.format(errorMessage, args);
        System.err.println(formattedMessage);
    }

}
