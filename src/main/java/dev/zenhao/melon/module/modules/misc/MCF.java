package dev.zenhao.melon.module.modules.misc;

import dev.zenhao.melon.manager.FriendManager;
import dev.zenhao.melon.module.Category;
import dev.zenhao.melon.module.Module;
import dev.zenhao.melon.utils.chat.ChatUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.RayTraceResult;
import org.lwjgl.input.Mouse;

@Module.Info(name = "MCF", description = "MCF", category = Category.MISC)
public class MCF extends Module {

    private boolean clicked;

    @Override
    public void onUpdate() {
        if (fullNullCheck()) {
            return;
        }
        if (mc.currentScreen == null) {
            if (Mouse.isButtonDown(2)) {
                if (!this.clicked) {
                    final RayTraceResult result = mc.objectMouseOver;
                    if (result != null && result.typeOfHit == RayTraceResult.Type.ENTITY) {
                        final Entity entity = result.entityHit;
                        if (entity instanceof EntityPlayer) {
                            String name = entity.getName();
                            if (FriendManager.isFriend(name)) {
                                FriendManager.removeFriend(name);
                                ChatUtil.sendMessage(ChatUtil.SECTIONSIGN + "b" + name + ChatUtil.SECTIONSIGN + "r" + " has been unfriended.");
                            } else {
                                FriendManager.addFriend(name);
                                ChatUtil.sendMessage(ChatUtil.SECTIONSIGN + "b" + name + ChatUtil.SECTIONSIGN + "r" + " has been friended.");
                            }
                        }
                    }
                }
                this.clicked = true;
            } else {
                this.clicked = false;
            }
        }
    }
}
