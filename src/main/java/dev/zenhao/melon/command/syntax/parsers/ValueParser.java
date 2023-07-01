package dev.zenhao.melon.command.syntax.parsers;

import dev.zenhao.melon.command.syntax.SyntaxChunk;
import dev.zenhao.melon.module.IModule;
import dev.zenhao.melon.module.ModuleManager;
import dev.zenhao.melon.setting.Setting;

import java.util.HashMap;
import java.util.TreeMap;

public class ValueParser
        extends AbstractParser {
    int moduleIndex;

    public ValueParser(int moduleIndex) {
        this.moduleIndex = moduleIndex;
    }

    @Override
    public String getChunk(SyntaxChunk[] chunks, SyntaxChunk thisChunk, String[] values2, String chunkValue) {
        if (this.moduleIndex > values2.length - 1 || chunkValue == null) {
            return this.getDefaultChunk(thisChunk);
        }
        String module = values2[this.moduleIndex];
        IModule m = ModuleManager.getModuleByName(module);
        if (m == null) {
            return "";
        }
        HashMap<String, Setting> possibilities = new HashMap<>();
        for (Setting v : m.getSettingList()) {
            if (!v.getName().toLowerCase().startsWith(chunkValue.toLowerCase())) continue;
            possibilities.put(v.getName(), v);
        }
        if (possibilities.isEmpty()) {
            return "";
        }
        TreeMap p = new TreeMap(possibilities);
        Setting aV = (Setting) p.firstEntry().getValue();
        return aV.getName().substring(chunkValue.length());
    }
}

