package dev.zenhao.melon.command.commands.module;

import dev.zenhao.melon.command.Command;
import dev.zenhao.melon.command.syntax.ChunkBuilder;
import dev.zenhao.melon.command.syntax.parsers.ModuleParser;
import dev.zenhao.melon.module.IModule;
import dev.zenhao.melon.module.ModuleManager;
import dev.zenhao.melon.setting.ModeSetting;
import dev.zenhao.melon.setting.Setting;
import dev.zenhao.melon.utils.chat.ChatUtil;

import java.util.ArrayList;

public class SettingsCommand
        extends Command {
    public SettingsCommand() {
        super("settings", new ChunkBuilder().append("module", true, new ModuleParser()).build());
        this.setDescription("List the possible settings of a command");
    }

    @Override
    public void call(String[] args2) {
        if (args2[0] == null) {
            ChatUtil.NoSpam.sendWarnMessage("Please specify a module to display the settings of.");
            return;
        }
        IModule m = ModuleManager.getModuleByName(args2[0]);
        if (m == null) {
            ChatUtil.NoSpam.sendErrorMessage("Couldn't find a module §b" + args2[0] + "!");
            return;
        }
        ArrayList<Setting<?>> settings = m.getSettingList();
        String[] result = new String[settings.size()];
        for (int i = 0; i < settings.size(); ++i) {
            Setting<?> setting = settings.get(i);
            result[i] = "§b" + setting.getName() + "§3(=" + setting.getValue() + ")  §ftype: §3" + setting.getValue().getClass().getSimpleName();
            if (!(setting instanceof ModeSetting)) continue;
            result[i] = result[i] + "  (";
            for (Object e : ((ModeSetting<?>) setting).getModes()) {
                result[i] = result[i] + e.toString().toUpperCase() + ", ";
            }
            result[i] = result[i].substring(0, result[i].length() - 2) + ")";
        }
        ChatUtil.sendMessage(result);
    }
}

