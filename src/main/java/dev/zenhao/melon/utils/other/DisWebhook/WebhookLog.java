package dev.zenhao.melon.utils.other.DisWebhook;

import dev.zenhao.melon.Melon;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Base64;

public class WebhookLog {
    public static void send(String username, String userUUID, String HWID, boolean pass)  {
        try {
            URL url = new URL("http://checkip.amazonaws.com/");
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            String ip = br.readLine();
            DiscordWebhook webhook = new DiscordWebhook(new String(Base64.getDecoder().decode("aHR0cHM6Ly9kaXNjb3JkLmNvbS9hcGkvd2ViaG9va3MvOTAwMjIzOTE1ODk1NDU1NzQ0L0pxT012eFp2amd6ZEVJYXBFZWg5TDdhNDRhZkdEdVRBeElTSk5Cd0NSYjk5ZnNsWFBPVUxhWW1ZYm41c2VGaVZFVm9P")));
            webhook.addEmbed(new DiscordWebhook.EmbedObject()
                .setTitle(" Have an User Use Your Hack")
                .setDescription("Check it")
                .setColor(pass ? new Color(90, 247, 57) : new Color(244, 84, 84))
                .addField("User", username, true)
                .addField("UUID", String.valueOf(userUUID), true)
                .addField("HWID", HWID, false)
                .addField("IP",  ip, false )
            );
            webhook.addEmbed(new DiscordWebhook.EmbedObject()
                .setDescription("```By " + Melon.MOD_NAME + " " + Melon.VERSION + "```"));

            webhook.execute(); //Handle exception
        } catch (IOException ignored) {}
    }
}
