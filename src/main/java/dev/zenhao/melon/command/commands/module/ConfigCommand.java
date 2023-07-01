package dev.zenhao.melon.command.commands.module;

import dev.zenhao.melon.command.Command;
import dev.zenhao.melon.command.syntax.ChunkBuilder;
import dev.zenhao.melon.command.syntax.parsers.DependantParser;
import dev.zenhao.melon.command.syntax.parsers.EnumParser;
import dev.zenhao.melon.manager.FileManager;
import dev.zenhao.melon.utils.chat.ChatUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by 086 on 14/10/2018.
 */
public class ConfigCommand extends Command {

    public ConfigCommand() {
        super("config", new ChunkBuilder()
                .append("mode", true, new EnumParser("reload", "save", "path"))
                .append("path", true, new DependantParser(0, new DependantParser.Dependency(new String[][]{{"path", "path"}}, "")))
                .build(), "cfg");
    }
    public static boolean org = false;

    @Override
    public void call(String[] args) {
        if (args[0] == null) {
            ChatUtil.NoSpam.sendErrorMessage("Missing argument &bmode&r: Choose from reload, save or path");
            return;
        }
        org = args[1].equals("org");
        switch (args[0].toLowerCase()) {
            case "reload":
                reload();
                break;
            case "save":
                try {
                    FileManager.saveAll(org);
                    ChatUtil.NoSpam.sendWarnMessage("Saved configuration!");
                } catch (Exception e) {
                    e.printStackTrace();
                    ChatUtil.NoSpam.sendErrorMessage("Failed to save ! " + e.getMessage());
                }
                break;
            case "path":
                if (args[1] == null) {
                    Path file = Paths.get(FileManager.MODULE_CONFIG);
                    ChatUtil.NoSpam.sendMessage("Path to configuration: &b" + file.toAbsolutePath());
                } else {
                    String newPath = args[1];
                    if (!isFilenameValid(newPath)) {
                        ChatUtil.NoSpam.sendErrorMessage("&b" + newPath + "&r is not a valid path");
                        break;
                    }
                    try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("KAMILastConfig.txt"))) {
                        writer.write(newPath);
                        reload();
                        ChatUtil.NoSpam.sendMessage("Configuration path set to &b" + newPath + "&r!");
                        break;
                    } catch (IOException e) {
                        e.printStackTrace();
                        ChatUtil.NoSpam.sendErrorMessage("Couldn't set path: " + e.getMessage());
                        break;
                    }
                }
                break;
            default:
                ChatUtil.NoSpam.sendWarnMessage("Incorrect mode, please choose from: reload, save or path");
        }
    }

    public static boolean isFilenameValid(String file) {
        File f = new File(file);
        try {
            f.getCanonicalPath();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private void reload() {
        FileManager.loadAll(ConfigCommand.org);
        ChatUtil.NoSpam.sendWarnMessage("Configuration reloaded!");
    }

}
