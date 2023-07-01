package dev.zenhao.melon.gui.alt.utils;

import com.mojang.authlib.Agent;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;
import com.mojang.realmsclient.gui.ChatFormatting;
import dev.zenhao.melon.Melon;
import dev.zenhao.melon.mixin.client.mc.MixinIMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;

import java.net.Proxy;

public final class AltLoginThread extends Thread {
    private final String password;
    private final String username;
    private final Minecraft mc = Minecraft.getMinecraft();
    private String status;

    public AltLoginThread(String username, String password) {
        super("Alt Login Thread");
        this.username = username;
        this.password = password;
        this.status = (ChatFormatting.GRAY) + "Waiting...";
    }

    private Session createSession(String username, String password) {
        YggdrasilAuthenticationService service = new YggdrasilAuthenticationService(Proxy.NO_PROXY, "");
        YggdrasilUserAuthentication auth = (YggdrasilUserAuthentication) service.createUserAuthentication(Agent.MINECRAFT);
        auth.setUsername(username);
        auth.setPassword(password);
        try {
            auth.logIn();
            //UUID -> Melon.logger.info(auth.getSelectedProfile().getId().toString());
            return new Session(auth.getSelectedProfile().getName(), auth.getSelectedProfile().getId().toString(), auth.getAuthenticatedToken(), "mojang");
        } catch (AuthenticationException localAuthenticationException) {
            localAuthenticationException.printStackTrace();
            return null;
        }
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public void run() {
        if (this.password.equals("")) {
            ((MixinIMinecraft) this.mc).setSession(new Session(this.username, "", "", "mojang"));
            this.status = (ChatFormatting.GREEN) + "Logged in. (" + this.username + " - offline name)";
            return;
        }
        this.status = (ChatFormatting.YELLOW) + "Logging in...";
        Session auth = this.createSession(this.username, this.password);
        if (auth == null) {
            this.status = (ChatFormatting.RED) + "Login failed!";
        } else {
            this.status = (ChatFormatting.GREEN) + "Logged in. (" + auth.getUsername() + ")";
            ((MixinIMinecraft) this.mc).setSession(auth);
        }
    }
}

