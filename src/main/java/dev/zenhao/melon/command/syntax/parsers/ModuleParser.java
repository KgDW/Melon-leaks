package dev.zenhao.melon.command.syntax.parsers;

import dev.zenhao.melon.command.syntax.SyntaxChunk;
import dev.zenhao.melon.command.syntax.parsers.AbstractParser;
import dev.zenhao.melon.module.IModule;
import dev.zenhao.melon.module.ModuleManager;

public class ModuleParser
extends AbstractParser {
    @Override
    public String getChunk(SyntaxChunk[] chunks, SyntaxChunk thisChunk, String[] values2, String chunkValue) {
        if (chunkValue == null) {
            return this.getDefaultChunk(thisChunk);
        }
        IModule chosen = ModuleManager.getModules().stream().filter(module -> module.getName().toLowerCase().startsWith(chunkValue.toLowerCase())).findFirst().orElse(null);
        if (chosen == null) {
            return null;
        }
        return chosen.getName();
    }
}

