package dev.zenhao.melon.module.modules.render;

import dev.zenhao.melon.event.events.client.PacketEvent;
import dev.zenhao.melon.event.events.render.RenderEvent;
import dev.zenhao.melon.module.Category;
import dev.zenhao.melon.module.Module;
import dev.zenhao.melon.utils.gl.MelonTessellator;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

@Module.Info(name = "NewChunks", category = Category.RENDER)
public class NewChunks extends Module {
    private final List<ChunkData> chunkDataList = new ArrayList<>();
    private final ICamera frustum = new Frustum();

    @Override
    public void onEnable() {
        if (fullNullCheck()) {
            return;
        }
        this.chunkDataList.clear();
    }

    @SubscribeEvent
    public void receivePacket(PacketEvent.Receive event) {
        if (fullNullCheck()) {
            return;
        }
        if (event.getPacket() instanceof SPacketChunkData) {
            final SPacketChunkData packet = (SPacketChunkData) event.getPacket();
            if (!packet.isFullChunk()) {
                final ChunkData chunk = new ChunkData(packet.getChunkX() * 16, 0, packet.getChunkZ() * 16);

                if (!this.chunkDataList.contains(chunk)) {
                    this.chunkDataList.add(chunk);
                }
            }
        }
    }

    @Override
    public void onWorldRender(RenderEvent event) {
        if (fullNullCheck()) {
            return;
        }
        for (ChunkData chunkData : new ArrayList<>(chunkDataList)) {
            Block block = mc.world.getBlockState(chunkData).getBlock();
            if (chunkData != null) {
                this.frustum.setPosition(mc.getRenderViewEntity().posX, mc.getRenderViewEntity().posY, mc.getRenderViewEntity().posZ);

                final AxisAlignedBB bb = new AxisAlignedBB(chunkData.x, 0, chunkData.z, chunkData.x + 16, 1, chunkData.z + 16);

                if (frustum.isBoundingBoxInFrustum(bb)) {
                    MelonTessellator.INSTANCE.drawPlane(chunkData.x - mc.getRenderManager().viewerPosX, -mc.getRenderManager().viewerPosY, chunkData.z - mc.getRenderManager().viewerPosZ, new AxisAlignedBB(0, 0, 0, 16, 1, 16), 1, 0xFF9900EE);
                }
            }
        }
    }

    public static class ChunkData extends BlockPos {
        private int x;
        private int y;
        private int z;

        public ChunkData(int x, int y, int z) {
            super(x, y, z);
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public int getZ() {
            return z;
        }

        public void setZ(int z) {
            this.z = z;
        }
    }
}
