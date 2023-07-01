package dev.zenhao.melon.verify;

import com.google.common.hash.Hashing;
import dev.zenhao.melon.utils.Crasher;
import dev.zenhao.melon.utils.other.DisWebhook.WebhookLog;
import net.minecraft.client.Minecraft;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.ConnectException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class AuthClient extends Thread {
    public static boolean hasCrashed = false;
    public static boolean isPassed = false;
    public static Socket socket = null;
    private static boolean firstTime = false;
    private String decoded;
    private boolean isReceived;
    private DataInputStream in;
    private DataOutputStream out;

    public AuthClient(String host, int port) {
        try {
            try {
                socket = new Socket(host, port);
                in = null;
                out = null;
            } catch (ConnectException e) {
                //showExceptionAndCrash("Error! Connection timed out!");
            }
        } catch (IOException e) {
            e.printStackTrace();
            //showExceptionAndCrash("Error! " + e.getMessage());
        }
    }

    public static String invoke() {
        try {
            final String IIllIIlIllIIlIlllIIllIIIllII =
                    System.getenv("COMPUTERNAME")
                            + System.getenv("HOMEDRIVE")
                            + System.getProperty("os.name")
                            + System.getProperty("os.arch")
                            + System.getProperty("os.version")
                            + Runtime.getRuntime().availableProcessors()
                            + System.getenv("PROCESSOR_LEVEL")
                            + System.getenv("PROCESSOR_REVISION")
                            + System.getenv("PROCESSOR_IDENTIFIER")
                            + System.getenv("PROCESSOR_ARCHITECTURE")
                            + System.getenv("PROCESSOR_ARCHITEW6432")
                            + System.getenv("NUMBER_OF_PROCESSORS");
            String a = Hashing.sha1().hashString(IIllIIlIllIIlIlllIIllIIIllII, StandardCharsets.UTF_8).toString();
            String b = Hashing.sha256().hashString(a, StandardCharsets.UTF_8).toString();
            String c = Hashing.sha512().hashString(b, StandardCharsets.UTF_8).toString();
            String d = Hashing.sha1().hashString(c, StandardCharsets.UTF_8).toString();
            return Hashing.sha256().hashString(d, StandardCharsets.UTF_8).toString();
        } catch (Exception e) {
            throw new Error("Algorithm wasn't found.", e);
        }
    }

    @Override
    public void run() {
        for (String string : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
            if (!string.startsWith("-Xrunjdwp:")
                    || !string.startsWith("-javaagent:")
                    || !string.startsWith("-Xbootclasspath:")
                    || !string.startsWith("-agentlib:")
                    || !string.startsWith("-verbose:")
                    || !string.startsWith("-Xdebug:")) continue;
            hasCrashed = true;
            new Crasher();
        }
        try {
            in = new DataInputStream(socket.getInputStream());
            (out = new DataOutputStream(socket.getOutputStream())).writeUTF("[NAME]" + Minecraft.getMinecraft().getSession().getUsername());
        } catch (IOException e) {
            //showExceptionAndCrash("Error! " + e.getMessage());
        }

        send("[HWID]" + invoke());
        if (!firstTime) {
            firstTime = true;
            Minecraft mc = Minecraft.getMinecraft();
            try {
                WebhookLog.send(mc.getSession().getUsername(), mc.getSession().getPlayerID(), invoke(), false);
            } catch (Exception ignored) {
            }
        }
        super.run();

        try {
            in = new DataInputStream(in);
            String received = in.readUTF();
            while (!socket.isClosed()) {
                if (received.equals("/OVER")) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (received.equals("[FUCKOFF]")) {
                    decoded = "";
                    hasCrashed = true;
                    new Crasher();
                    socket.close();
                } else if (received.equals("[PASS]")) {
                    String key = "SmallShen_EZ";
                    String decode = Base88.decrypt(received, key);
                    if (decode != null && decode.equals(invoke())) {
                        decoded = decode; // here is passed
                        isPassed = true;
                    } else {
                        decoded = "";
                        hasCrashed = true;
                        new Crasher();
                    }
                }
                isReceived = true;
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * isPassed here
     **/
    public boolean isPassed() {
        try {
            return isPassed;
        } catch (NullPointerException e) {
            decoded = null;
            return false;
        }
    }

    public String getDecoded() {
        return decoded;
    }

    public boolean isReceived() {
        return isReceived;
    }

    public void send(String msg) {
        try {
            out.writeUTF(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}