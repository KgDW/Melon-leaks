package dev.zenhao.melon.command.commands.mc;

import dev.zenhao.melon.command.Command;
import dev.zenhao.melon.command.syntax.ChunkBuilder;
import dev.zenhao.melon.utils.chat.ChatUtil;

public class SayCommand
extends Command {
    public SayCommand() {
        super("say", new ChunkBuilder().append("message").build());
        this.setDescription("Allows you to send any message, even with a prefix in it");
    }

    @Override
    public void call(String[] args2) {
        StringBuilder message = new StringBuilder();
        for (String arg : args2) {
            if (arg == null) continue;
            message.append(" ").append(arg);
        }
        ChatUtil.sendServerMessage(message.toString());
    }
}

