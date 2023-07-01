package dev.zenhao.melon.module.modules.render;

import com.mojang.realmsclient.gui.ChatFormatting;
import dev.zenhao.melon.event.events.render.RenderEvent;
import dev.zenhao.melon.module.Category;
import dev.zenhao.melon.module.Module;
import dev.zenhao.melon.setting.Setting;
import dev.zenhao.melon.utils.chat.ChatUtil;
import dev.zenhao.melon.utils.gl.MelonTessellator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.util.*;

@Module.Info(name = "PearlViewer", category = Category.RENDER)
public class PearlViewer extends Module {
    private final HashMap<UUID, List<Vec3d>> poses = new HashMap<>();
    private final HashMap<UUID, Double> time = new HashMap<>();
    private final Setting<Boolean> chat = bsetting("Chat", true);
    private final Setting<Boolean> render = bsetting("Render", true);
    private final Setting<Double> renderTime = dsetting("RenderTime", 5, 0, 30);
    private final Setting<Integer> Thick = isetting("Thick", 3, 0, 10);

    @Override
    public void onUpdate() {
        if (fullNullCheck()) {
            return;
        }
        Iterator<?> iter = (new HashMap<>(this.time)).entrySet().iterator();

        while (iter.hasNext()) {
            Map.Entry<UUID, Double> e = (Map.Entry<UUID, Double>) iter.next();

            if (e.getValue() <= 0.0D) {
                this.poses.remove(e.getKey());
                this.time.remove(e.getKey());
            } else {
                this.time.replace(e.getKey(), e.getValue() - 0.05D);
            }
        }

        iter = mc.world.loadedEntityList.iterator();

        while (true) {
            Entity e;
            do {
                if (!iter.hasNext()) {
                    return;
                }

                e = (Entity) iter.next();
            } while (!(e instanceof EntityEnderPearl));

            if (!this.poses.containsKey(e.getUniqueID())) {
                if (chat.getValue()) {
                    for (EntityPlayer entityPlayer : new ArrayList<>(mc.world.playerEntities)) {
                        if (entityPlayer.getDistance(e) < 4.0F && !((Entity) entityPlayer).getName().equals(mc.player.getName())) {
                            ChatUtil.sendMessage(ChatFormatting.RED + entityPlayer.getName() + ChatFormatting.AQUA + " Threw a Pearl !");
                            break;
                        }
                    }
                }

                this.poses.put(e.getUniqueID(), new ArrayList<>(Collections.singletonList(e.getPositionVector())));
                this.time.put(e.getUniqueID(), this.renderTime.getValue());
            } else {
                this.time.replace(e.getUniqueID(), this.renderTime.getValue());
                List<Vec3d> v = this.poses.get(e.getUniqueID());
                v.add(e.getPositionVector());
            }
        }
    }

    @Override
    public void onWorldRender(RenderEvent event) {
        if (fullNullCheck()) {
            return;
        }

        if (this.render.getValue()) {
            MelonTessellator.INSTANCE.prepare(GL11.GL_QUADS);
            Iterator<Map.Entry<UUID, List<Vec3d>>> posIter = this.poses.entrySet().iterator();

            while (true) {
                Map.Entry<UUID, List<Vec3d>> e;
                do {
                    if (!posIter.hasNext()) {
                        MelonTessellator.release();
                        return;
                    }

                    e = posIter.next();
                } while (e.getValue().size() <= 2);

                GL11.glBegin(1);
                Random rand = new Random(e.getKey().hashCode());
                double r = 0.5D + rand.nextDouble() / 2.0D;
                double g = 0.5D + rand.nextDouble() / 2.0D;
                double b = 0.5D + rand.nextDouble() / 2.0D;
                GL11.glColor3d(r, g, b);
                double[] rPos = MelonTessellator.rPos();
                for (int i = 1; i < e.getValue().size(); ++i) {
                    GL11.glVertex3d(e.getValue().get(i).x - rPos[0], e.getValue().get(i).y - rPos[1], e.getValue().get(i).z - rPos[2]);
                    GL11.glVertex3d(e.getValue().get(i - 1).x - rPos[0], e.getValue().get(i - 1).y - rPos[1], e.getValue().get(i - 1).z - rPos[2]);
                }
                GL11.glEnd();
            }
        }
    }

}
