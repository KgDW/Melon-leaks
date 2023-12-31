package dev.zenhao.melon.inject;

import dev.zenhao.melon.Melon;
import net.minecraft.launchwrapper.LaunchClassLoader;

import java.lang.instrument.Instrumentation;

public class AgentMain {
    public static void agentmain(String args, Instrumentation instrumentation) throws Exception {
        for (Class<?> classes : instrumentation.getAllLoadedClasses()) {
            if (classes.getName().startsWith("net.minecraft.client.Minecraft")) {
                LaunchClassLoader classLoader = (LaunchClassLoader)classes.getClassLoader();
                classLoader.addURL(AgentMain.class.getProtectionDomain().getCodeSource().getLocation());
                Class<?> client = classLoader.loadClass(Melon.class.getName());
                client.newInstance();
            }
        }
    }
}