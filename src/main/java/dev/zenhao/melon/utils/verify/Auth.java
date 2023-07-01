package dev.zenhao.melon.utils.verify;

import dev.zenhao.melon.utils.Crasher;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class Auth {
    public static boolean hasCrashed = false;
    public static boolean isPassed = false;
    public static Socket socket = null;
    private static DataInputStream in;
    private static DataOutputStream out;

    public static void init() {
        try {
            socket = new Socket("150.138.72.217", 19198);
            in = null;
            out = null;
        } catch (Exception ignored) {
        }
    }

    public static String getHWID() {
        return "[CAONIMA]" + c(System.getenv("PROCESSOR_IDENTIFIER") + System.getenv("COMPUTERNAME") + System.getenv("PROCESSOR_ARCHITECTURE") + System.getenv("TEMP") + System.getProperty("user.name"));
    }

    private static String c(final String text) {
        try {
            final MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(text.getBytes(StandardCharsets.ISO_8859_1), 0, text.length());
            byte[] sha1hash = md.digest();
            return d(sha1hash);
        } catch (Exception ignored) {
        }
        return null;
    }

    private static String d(final byte[] data) {
        final StringBuilder buf = new StringBuilder();
        for (byte datum : data) {
            int halfbyte = datum >>> 4 & 0xF;
            int two_halfs = 0;
            do {
                if (halfbyte <= 9) {
                    buf.append((char) (48 + halfbyte));
                } else {
                    buf.append((char) (97 + (halfbyte - 10)));
                }
                halfbyte = (datum & 0xF);
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }

    public void run() {
        for (String string : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
            if (!string.startsWith("-Xrunjdwp:")
                    || !string.startsWith("-javaagent:")
                    || !string.startsWith("-Xbootclasspath:")
                    || !string.startsWith("-agentlib:")
                    || !string.startsWith("-verbose:")
                    || !string.startsWith("-Xdebug:")) continue;
            hasCrashed = true;
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (Exception ignored) {
            }
            new Crasher();
        }
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            //showExceptionAndCrash("Error! " + e.getMessage());
        }

        send(getHWID());
        try {
            in = new DataInputStream(in);
            String received = in.readUTF();
            while (!socket.isClosed()) {
                if (received.equals("[SUCCESSFUL]")) {
                    isPassed = true;
                } else {
                    hasCrashed = true;
                    socket.close();
                    new Crasher();
                    return;
                }
                socket.close();
            }
        } catch (IOException ignored) {
        }
    }

    public boolean isPassed() {
        try {
            return isPassed;
        } catch (NullPointerException e) {
            return false;
        }
    }

    public void send(String msg) {
        try {
            out.writeUTF(msg);
        } catch (Exception ignored) {
        }
    }
}