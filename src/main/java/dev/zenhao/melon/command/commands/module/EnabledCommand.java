package dev.zenhao.melon.command.commands.module;

import dev.zenhao.melon.command.Command;
import dev.zenhao.melon.module.IModule;
import dev.zenhao.melon.module.ModuleManager;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

import dev.zenhao.melon.utils.chat.ChatUtil;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.StringUtils;

public class EnabledCommand
extends Command {
    public EnabledCommand() {
        super("enabled", null);
        this.setDescription("Prints enabled modules");
    }

    @Override
    public void call(String[] args2) {
        AtomicReference<String> enabled = new AtomicReference<>("");
        ArrayList<IModule> mods = new ArrayList<>(ModuleManager.getModules());
        mods.forEach(module -> {
            if (module.isEnabled()) {
                enabled.set(enabled + module.getName() + ", ");
            }
        });
        enabled.set(StringUtils.chop(StringUtils.chop(String.valueOf(enabled))));
        ChatUtil.NoSpam.sendWarnMessage("Enabled modules: \n" + TextFormatting.GRAY + enabled);
    }
}

