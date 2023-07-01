package dev.zenhao.melon.utils.verify;

import dev.zenhao.melon.utils.ClassInvoke;
import dev.zenhao.melon.utils.Crasher;
import dev.zenhao.melon.utils.NetworkDump;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Verify {
    public static void use() {
        ClassInvoke.INSTANCE.addClass(NetworkDump.class, "Dump");
        ClassInvoke.INSTANCE.addClass(Auth.class, "init");
        ClassInvoke.INSTANCE.addClass(Auth.class, "run");
    }

    public static void init() {
        try {
            Socket socket = new Socket("150.138.72.217", 19198);
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            if (dos != null) {
                dos.writeUTF(getHWID());
            }
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            if (!dis.readUTF().equals("[SUCCESSFUL]")) {
                socket.close();
                ClassInvoke.INSTANCE.addClass(Crasher.class, "shutdownHard");
                return;
            }
            dos.flush();
            dos.close();
            dis.close();
            socket.close();
        } catch (Exception ignored) {
        }
    }

    public static String getHWID() throws Exception {
        return "[CAONIMA]" + c(System.getenv("PROCESSOR_IDENTIFIER") + System.getenv("COMPUTERNAME") + System.getenv("PROCESSOR_ARCHITECTURE") + System.getenv("TEMP") + System.getProperty("user.name"));
    }

    private static String c(final String text) throws NoSuchAlgorithmException {
        final MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(text.getBytes(StandardCharsets.ISO_8859_1), 0, text.length());
        byte[] sha1hash = md.digest();
        return d(sha1hash);
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
}
