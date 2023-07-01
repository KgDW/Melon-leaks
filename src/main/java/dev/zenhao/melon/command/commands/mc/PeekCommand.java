package dev.zenhao.melon.command.commands.mc;

import dev.zenhao.melon.command.Command;
import dev.zenhao.melon.command.syntax.SyntaxChunk;
import dev.zenhao.melon.utils.chat.ChatUtil;
import dev.zenhao.melon.utils.Wrapper;
import net.minecraft.block.Block;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityShulkerBox;

public class PeekCommand
extends Command {
    public static TileEntityShulkerBox sb;

    public PeekCommand() {
        super("peek", SyntaxChunk.EMPTY);
        this.setDescription("Look inside the contents of a shulker box without opening it");
    }

    @Override
    public void call(String[] args2) {
        ItemStack is = Wrapper.getPlayer().inventory.getCurrentItem();
        if (is.getItem() instanceof ItemShulkerBox) {
            Test entityBox = new Test();
            entityBox.setBlockType(((ItemShulkerBox)is.getItem()).getBlock());
            entityBox.setWorld(Wrapper.getWorld());
            assert is.getTagCompound() != null;
            entityBox.readFromNBT(is.getTagCompound().getCompoundTag("BlockEntityTag"));
            sb = entityBox;
        } else {
            ChatUtil.NoSpam.sendWarnMessage("You aren't carrying a shulker box.");
        }
    }

    private static class Test
    extends TileEntityShulkerBox {
        private Test() {
        }

        public void setBlockType(Block blockType) {
            this.blockType = blockType;
        }
    }
}

