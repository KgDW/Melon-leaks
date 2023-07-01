package dev.zenhao.melon.command.commands.module;

import dev.zenhao.melon.command.Command;
import dev.zenhao.melon.utils.chat.ChatUtil;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class ChestGUI extends Command {
    public ChestGUI() {
        super("cui");
        this.setDescription("Create a chest GUI");
    }

    @Nullable
    public ILockableContainer getLockableContainer(World worldIn, BlockPos pos) {
        TileEntity tileentity = worldIn.getTileEntity(pos);
        return (ILockableContainer) tileentity;
    }

    @Override
    public void call(String[] var1) {
        try {
            ILockableContainer ilockablecontainer = this.getLockableContainer(mc.world, mc.player.getPosition());
            if (ilockablecontainer != null) {
                GuiChest az = new GuiChest(mc.player.inventory, ilockablecontainer);
                mc.player.displayGUIChest((IInventory) az);
            } else {
                ChatUtil.sendMessage("Failed!");
            }
        } catch (Exception e) {
            ChatUtil.sendMessage(e.getMessage());
        }
    }
}
