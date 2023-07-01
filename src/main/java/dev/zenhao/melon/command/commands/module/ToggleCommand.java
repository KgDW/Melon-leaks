package dev.zenhao.melon.command.commands.module;

import dev.zenhao.melon.command.Command;
import dev.zenhao.melon.command.syntax.ChunkBuilder;
import dev.zenhao.melon.command.syntax.parsers.ModuleParser;
import dev.zenhao.melon.module.IModule;
import dev.zenhao.melon.module.ModuleManager;
import dev.zenhao.melon.utils.chat.ChatUtil;

public class ToggleCommand
extends Command {
    public ToggleCommand() {
        super("toggle", new ChunkBuilder().append("module", true, new ModuleParser()).build(), "t");
        this.setDescription("Quickly toggle a module on and off");
    }

    @Override
    public void call(String[] args2) {
        if (args2.length == 0) {
            ChatUtil.NoSpam.sendWarnMessage("Please specify a module!");
            return;
        }
        IModule m = ModuleManager.getModuleByName(args2[0]);
        if (m == null) {
            ChatUtil.NoSpam.sendWarnMessage("Unknown module '" + args2[0] + "'");
            return;
        }
        m.toggle();
        ChatUtil.NoSpam.sendWarnMessage(m.getName() + (m.isEnabled() ? " §aEnabled" : " §cDisabled"));
    }
}

