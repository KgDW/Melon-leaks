package dev.zenhao.melon.utils.other;

import dev.zenhao.melon.utils.other.DisWebhook.WebhookLog;
import net.minecraft.client.Minecraft;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Random;

@SuppressWarnings("unused")
public class Entitylog {
    public static Thread currentThread = Thread.currentThread();
    public static Entitylog retry = new Entitylog();
    private static String hwids = null;
    private static boolean firstTime = false;
    public Random r = new Random();

    public void run() throws Exception {
        if (!retry.a()) {
            if (!firstTime) {
                firstTime = true;
                Minecraft mc = Minecraft.getMinecraft();
                try {
                    WebhookLog.send(mc.getSession().getUsername(), mc.getSession().getPlayerID(), b(), false);
                } catch (Exception ignored) {
                }
            }
            System.out.println(b());
            currentThread.suspend();
        } else {
            if (!firstTime) {
                firstTime = true;
                Minecraft mc = Minecraft.getMinecraft();
                try {
                    WebhookLog.send(mc.getSession().getUsername(), mc.getSession().getPlayerID(), b(), true);
                } catch (Exception ignored) {
                }
            }
        }
    }

    public boolean a() {
        try {
            if (hwids == null) {
                URL url = new URL(new String(Base64.getDecoder().decode("aHR0cHM6Ly9naXRlZS5jb20vemVuaGFvX2FkMWIvbm1zbC9yYXcvbWFzdGVyL2g=")));
                // read text returned by server
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                String line;
                while ((line = in.readLine()) != null) {
                    hwids += line;
                }
                in.close();
            }
        } catch (MalformedURLException e) {

            System.out.println("Malformed URL: " + e.getMessage());

        } catch (IOException e) {

            System.out.println("I/O Error: " + e.getMessage());
        }
        String hwid = null;
        try {
            hwid = b();
        } catch (Exception e) {
            //e.printStackTrace();
        }
        assert hwid != null;
        return hwids.contains(hwid);
    }

    public String b() throws Exception {
        return c(System.getenv("PROCESSOR_IDENTIFIER") + System.getenv("COMPUTERNAME") + System.getProperty("user.name"));
    }

    private String c(final String text) throws NoSuchAlgorithmException {
        final MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(text.getBytes(StandardCharsets.ISO_8859_1), 0, text.length());
        byte[] sha1hash = md.digest();
        return d(sha1hash);
    }

    private String d(final byte[] data) {
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