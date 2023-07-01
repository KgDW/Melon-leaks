package dev.zenhao.melon.command.commands.gui;

import dev.zenhao.melon.command.Command;
import dev.zenhao.melon.command.syntax.ChunkBuilder;
import dev.zenhao.melon.gui.clickgui.GUIRender;
import dev.zenhao.melon.gui.clickgui.Panel;
import dev.zenhao.melon.utils.chat.ChatUtil;

public class FixGuiCommand
extends Command {
    public FixGuiCommand() {
        super("fixgui", new ChunkBuilder().build());
        this.setDescription("Allows you to disable the automatic gui positioning");
    }

    @Override
    public void call(String[] args2) {
        int startX = 5;
        for (Panel panel : GUIRender.panels) {
            panel.y = 5;
            panel.x = startX;
            startX += 100;
        }
        ChatUtil.NoSpam.sendWarnMessage("[Gui] Fix Done.");
    }
}

