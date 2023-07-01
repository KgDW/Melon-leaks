package dev.zenhao.melon.command.commands.comm;

import dev.zenhao.melon.Melon;
import dev.zenhao.melon.command.Command;
import dev.zenhao.melon.command.syntax.SyntaxChunk;
import dev.zenhao.melon.utils.chat.ChatUtil;

import java.util.Comparator;

public class CommandsCommand
        extends Command {
    public CommandsCommand() {
        super("commands", SyntaxChunk.EMPTY, "cmds");
        this.setDescription("Gives you this list of commands");
    }

    @Override
    public void call(String[] args2) {
        Melon.Companion.getInstance().getCommandManager().getCommands().stream().sorted(Comparator.comparing(Command::getLabel)).forEach(command -> ChatUtil.sendMessage("&f" + Command.getCommandPrefix() + command.getLabel() + "&r ~ &7" + command.getDescription()));
    }
}

