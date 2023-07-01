package dev.zenhao.melon.module.modules.misc

import com.mojang.authlib.GameProfile
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.setting.Setting
import dev.zenhao.melon.utils.entity.EntityUtil
import melon.utils.concurrent.threads.runSafe
import net.minecraft.client.entity.EntityOtherPlayerMP
import java.util.*

@Module.Info(name = "FakePlayer", category = Category.MISC, description = "Spawns a fake Player")
object FakePlayer : Module() {
    var health: Setting<Int> = isetting("Health", 12, 0, 36)
    var awa: Setting<String> = ssetting("Name", "Ab_noJB")
    override fun getHudInfo(): String? {
        return awa.value
    }

    override fun onEnable() {
        if (EntityUtil.nullCheck()) {
            return
        }
        val fakePlayer = EntityOtherPlayerMP(
            mc.world,
            GameProfile(UUID.fromString("60569353-f22b-42da-b84b-d706a65c5ddf"), awa.value)
        )
        fakePlayer.copyLocationAndAnglesFrom(mc.player)
        for (potionEffect in mc.player.activePotionEffects) {
            fakePlayer.addPotionEffect(potionEffect)
        }
        fakePlayer.health = health.value.toFloat()
        fakePlayer.inventory.copyInventory(mc.player.inventory)
        fakePlayer.rotationYawHead = mc.player.rotationYawHead
        mc.world.addEntityToWorld(-100, fakePlayer)
    }

    override fun onDisable() {
        runSafe {
            world.removeEntityFromWorld(-100)
        }
    }
}