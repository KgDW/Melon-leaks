package dev.zenhao.melon.command.commands;

import dev.zenhao.melon.command.Command;
import dev.zenhao.melon.command.syntax.ChunkBuilder;
import dev.zenhao.melon.command.syntax.parsers.EnumParser;
import dev.zenhao.melon.manager.FriendManager;
import dev.zenhao.melon.utils.chat.ChatUtil;
import dev.zenhao.melon.utils.other.Friend;

public class FriendCommand
        extends Command {
    public FriendCommand() {
        super("friend", new ChunkBuilder().append("mode", true, new EnumParser("add", "del")).append("name").build(), "f");
        this.setDescription("Add someone as your friend!");
    }

    @Override
    public void call(String[] args2) {
        if (args2[0] == null) {
            if (FriendManager.INSTANCE.friends.isEmpty()) {
                ChatUtil.NoSpam.sendWarnMessage("You currently don't have any friends added. friend add <name> to add one.");
                return;
            }
            StringBuilder f = new StringBuilder();
            for (Friend friend : FriendManager.INSTANCE.friends) {
                f.append(friend.name).append(", ");
            }
            f = new StringBuilder(f.substring(0, f.length() - 2));
            ChatUtil.sendMessage("Your friends: " + f);
            return;
        }
        if (args2[1] == null) {
            ChatUtil.NoSpam.sendMessage(String.format(FriendManager.isFriend(args2[0]) ? "Yes, %s is your friend." : "No, %s isn't a friend of yours.", args2[0]));
            return;
        }
        if (args2[0].equalsIgnoreCase("add") || args2[0].equalsIgnoreCase("new")) {
            if (FriendManager.isFriend(args2[1])) {
                ChatUtil.NoSpam.sendWarnMessage("That player is already your friend.");
                return;
            }
            new Thread(() -> {
                Friend f = new Friend(args2[1], true);
                if (f == null) {
                    ChatUtil.NoSpam.sendErrorMessage("Failed to find UUID of " + args2[1]);
                    return;
                }
                FriendManager.INSTANCE.friends.add(f);
                ChatUtil.NoSpam.sendMessage(ChatUtil.SECTIONSIGN + "b " + f.name + " has been friended.");
            }).start();
            return;
        }
        if (args2[0].equalsIgnoreCase("del") || args2[0].equalsIgnoreCase("remove") || args2[0].equalsIgnoreCase("delete")) {
            if (!FriendManager.isFriend(args2[1])) {
                ChatUtil.NoSpam.sendWarnMessage("That player isn't your friend.");
                return;
            }
            Friend friend = FriendManager.INSTANCE.friends.stream().filter(friend1 -> friend1.name.equalsIgnoreCase(args2[1])).findFirst().get();
            FriendManager.INSTANCE.friends.remove(friend);
            ChatUtil.NoSpam.sendWarnMessage(ChatUtil.SECTIONSIGN + "b " + friend.name + " has been unfriended.");
            return;
        }
        ChatUtil.NoSpam.sendWarnMessage("Please specify either add or remove");
    }
}

