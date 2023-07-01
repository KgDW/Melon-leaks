package dev.zenhao.melon.command.commands.module;

import dev.zenhao.melon.command.Command;
import dev.zenhao.melon.command.syntax.ChunkBuilder;
import dev.zenhao.melon.module.IModule;
import dev.zenhao.melon.module.ModuleManager;
import dev.zenhao.melon.utils.chat.ChatUtil;

public class DescriptionCommand
extends Command {
    public DescriptionCommand() {
        super("description", new ChunkBuilder().append("module").build(), "tooltip");
        this.setDescription("Prints a module's description into the chat");
    }

    @Override
    public void call(String[] args2) {
        for (String s : args2) {
            if (s == null) continue;
            IModule module = ModuleManager.getModuleByName(s);
            if (module != null){
                ChatUtil.sendMessage(module.getName() + "Description: &7" + module.description);
            }
        }
    }
}

