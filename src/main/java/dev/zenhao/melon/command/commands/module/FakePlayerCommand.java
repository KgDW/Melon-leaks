package dev.zenhao.melon.command.commands.module;

import dev.zenhao.melon.command.Command;
import dev.zenhao.melon.module.IModule;
import dev.zenhao.melon.module.ModuleManager;
import dev.zenhao.melon.utils.chat.ChatUtil;
import net.minecraft.client.gui.inventory.GuiChest;

public class FakePlayerCommand
extends Command {
    public FakePlayerCommand() {
        super("fp");
        this.setDescription("Quickly toggle a module on and off");
    }

    @Override
    public void call(String[] args2) {
        if (args2.length == 0) {
            ChatUtil.NoSpam.sendWarnMessage("Please specify a module!");
            return;
        }
        IModule m = ModuleManager.getModuleByName("FakePlayer");
        m.toggle();
        ChatUtil.NoSpam.sendWarnMessage(m.getName() + (m.isEnabled() ? ChatUtil.SECTIONSIGN + "a"+ " Enabled" : " §cDisabled"));
    }
}

