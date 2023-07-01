package dev.zenhao.melon.command.commands;

import dev.zenhao.melon.command.Command;
import dev.zenhao.melon.utils.chat.ChatUtil;

public class CreditsCommand
extends Command {
    public CreditsCommand() {
        super("credits", null);
        this.setDescription("Prints KAMI Blue's authors and contributors");
    }

    @Override
    public void call(String[] args2) {
        ChatUtil.sendMessage("\nName (Github if not same as name)" +
                "\n&l&9Author:" +
                "\nPyWong_921" +
                "\n&l&9Contributors:" +
                "\nZenHao_123");
    }
}

