package dev.zenhao.melon.mixin.client;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Map;
import java.util.Random;

@Mixin(value = {Explosion.class}, priority = Integer.MAX_VALUE)
public class MixinExplosion {
    @Final
    @Shadow
    public boolean causesFire;
    @Final
    @Shadow
    public boolean damagesTerrain;
    @Final
    @Shadow
    public Random random;
    @Final
    @Shadow
    public World world;
    @Final
    @Shadow
    public double x;
    @Final
    @Shadow
    public double y;
    @Final
    @Shadow
    public double z;
    @Final
    @Shadow
    public Entity exploder;
    @Final
    @Shadow
    public float size;
    @Final
    @Shadow
    public List<BlockPos> affectedBlockPositions;
    @Final
    @Shadow
    public Map<EntityPlayer, Vec3d> playerKnockbackMap;
    @Final
    @Shadow
    public Vec3d position;
}
