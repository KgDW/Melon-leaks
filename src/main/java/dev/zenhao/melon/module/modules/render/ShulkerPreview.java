package dev.zenhao.melon.module.modules.render;

import dev.zenhao.melon.event.events.client.PacketEvent;
import dev.zenhao.melon.event.events.render.EventRenderTooltip;
import dev.zenhao.melon.module.Category;
import dev.zenhao.melon.module.Module;
import dev.zenhao.melon.setting.Setting;
import dev.zenhao.melon.utils.chat.ChatUtil;
import dev.zenhao.melon.utils.render.RenderUtils3D;
import net.minecraft.block.Block;
import net.minecraft.block.BlockShulkerBox;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiShulkerBox;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketOpenWindow;
import net.minecraft.network.play.server.SPacketWindowItems;
import net.minecraft.tileentity.TileEntityShulkerBox;
import net.minecraft.util.NonNullList;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Mouse;

import java.util.*;

@Module.Info(name = "ShulkerPreview", category = Category.RENDER, description = "Previews shulkers in the game GUI")
public class ShulkerPreview extends Module {

    public final ArrayList<ItemStack> EnderChestItems = new ArrayList<>();
    public final HashMap<String, List<ItemStack>> SavedShulkerItems = new HashMap<>();
    public final Timer timer = new Timer();
    public Setting<Boolean> middleClick = bsetting("MidClick", true);
    public Setting<?> Mode = msetting("Mode", Modes.Normal);
    public boolean clicked;
    public int EnderChestWindowId = -1;
    public int ShulkerWindowId = -1;
    public String LastWindowTitle = "";

    @SubscribeEvent
    public void render(EventRenderTooltip event) {
        if (event.getItemStack() == null)
            return;

        final Minecraft mc = Minecraft.getMinecraft();

        if (Item.getIdFromItem(event.getItemStack().getItem()) == 130) {
            // store mouse/event coords
            int x = event.getX();
            int y = event.getY();

            // translate to mouse x, y
            GlStateManager.translate(x + 10, y - 5, 0);

            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            // background
            RenderUtils3D.drawRect(-3, -mc.fontRenderer.FONT_HEIGHT - 4, 9 * 16 + 3, 3 * 16 + 3, 0x99101010);
            RenderUtils3D.drawRect(-2, -mc.fontRenderer.FONT_HEIGHT - 3, 9 * 16 + 2, 3 * 16 + 2, 0xFF202020);
            RenderUtils3D.drawRect(0, 0, 9 * 16, 3 * 16, 0xFF101010);

            // text
            mc.fontRenderer.drawStringWithShadow("Melon EnderChest Viewer", 0, -mc.fontRenderer.FONT_HEIGHT - 1,
                    0xFFFFFFFF);

            GlStateManager.enableDepth();
            mc.getRenderItem().zLevel = 150.0F;
            RenderHelper.enableGUIStandardItemLighting();

            for (int i = 0; i < EnderChestItems.size(); ++i) {
                ItemStack itemStack = EnderChestItems.get(i);
                if (itemStack == null)
                    continue;

                // salhack.INSTANCE.logChat("Item: " + itemStack.getDisplayName());

                int offsetX = (i % 9) * 16;
                int offsetY = (i / 9) * 16;
                mc.getRenderItem().renderItemAndEffectIntoGUI(itemStack, offsetX, offsetY);
                mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRenderer, itemStack, offsetX, offsetY, null);
            }

            event.setCanceled(true);

            RenderHelper.disableStandardItemLighting();
            mc.getRenderItem().zLevel = 0.0F;
            GlStateManager.enableLighting();

            // reverse the translate
            GlStateManager.translate(-(x + 10), -(y - 5), 0);

            if (this.middleClick.getValue()) {
                if (Mouse.isButtonDown(2)) {
                    if (!this.clicked) {
                        InventoryBasic l_Inventory = new InventoryBasic("Melon EnderChest Viewer", true, 27);

                        for (ItemStack itemStack : EnderChestItems) {
                            if (itemStack == null)
                                continue;

                            l_Inventory.addItem(itemStack);
                        }

                        mc.displayGuiScreen(new GuiChest(mc.player.inventory, l_Inventory));
                    }
                    this.clicked = true;
                } else {
                    this.clicked = false;
                }
            }
        } else if (event.getItemStack().getItem() instanceof ItemShulkerBox) {
            if (Mode.getValue() == Modes.Normal)
                RenderLegacyShulkerPreview(event);
            else if (Mode.getValue() == Modes.DropPacket || Mode.getValue() == Modes.Inventory)
                Render2b2tShulkerPreview(event);
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SPacketWindowItems) {
            final SPacketWindowItems l_Packet = (SPacketWindowItems) event.getPacket();

            if (l_Packet.getWindowId() == EnderChestWindowId) {
                EnderChestItems.clear();

                for (int i = 0; i < l_Packet.getItemStacks().size(); ++i) {
                    ItemStack itemStack = l_Packet.getItemStacks().get(i);
                    if (itemStack == null)
                        continue;

                    if (i > 26)
                        break;

                    EnderChestItems.add(itemStack);
                }
            } else if (l_Packet.getWindowId() == ShulkerWindowId) {
                SavedShulkerItems.remove(LastWindowTitle);

                ArrayList<ItemStack> l_List = new ArrayList<>();

                for (int i = 0; i < l_Packet.getItemStacks().size(); ++i) {
                    ItemStack itemStack = l_Packet.getItemStacks().get(i);
                    if (itemStack == null)
                        continue;

                    if (i > 26)
                        break;

                    l_List.add(itemStack);
                }

                SavedShulkerItems.put(LastWindowTitle, l_List);
            }
        } else if (event.getPacket() instanceof SPacketOpenWindow) {
            final SPacketOpenWindow l_Packet = (SPacketOpenWindow) event.getPacket();

            if (l_Packet.getWindowTitle().getFormattedText().startsWith("Ender")) {
                EnderChestWindowId = l_Packet.getWindowId();
            } else {
                ShulkerWindowId = l_Packet.getWindowId();
                LastWindowTitle = l_Packet.getWindowTitle().getUnformattedText();
            }
        }
    }

    public NBTTagCompound getShulkerNBT(ItemStack stack) {
        NBTTagCompound compound = stack.getTagCompound();
        if (compound != null && compound.hasKey("BlockEntityTag", 10)) {
            NBTTagCompound tags = compound.getCompoundTag("BlockEntityTag");
            if (tags.hasKey("Items", 9))
                return tags;
        }

        return null;
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @SubscribeEvent
    public void onWorldEvent(EntityJoinWorldEvent event) {
        if (Mode.getValue() != Modes.DropPacket)
            return;

        if (event.getEntity() == null || !(event.getEntity() instanceof EntityItem))
            return;

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                EntityItem l_Item = (EntityItem) event.getEntity();

                if (!(l_Item.getItem().getItem() instanceof ItemShulkerBox))
                    return;

                ItemStack shulker = l_Item.getItem();

                NBTTagCompound shulkerNBT = getShulkerNBT(shulker);

                if (shulkerNBT != null) {
                    TileEntityShulkerBox fakeShulker = new TileEntityShulkerBox();
                    fakeShulker.loadFromNbt(shulkerNBT);
                    String customName = shulker.getDisplayName();
                    ArrayList<ItemStack> l_Items = new ArrayList<>();

                    int l_SlotsNotEmpty = 0;

                    for (int i = 0; i < 27; ++i) {
                        l_Items.add(fakeShulker.getStackInSlot(i));

                        if (fakeShulker.getStackInSlot(i) != ItemStack.EMPTY)
                            ++l_SlotsNotEmpty;
                    }

                    if (SavedShulkerItems.containsKey(customName))
                        SavedShulkerItems.remove(customName);
                    else
                        ChatUtil.sendMessage("New shulker found with name " + customName + " it contains " + l_SlotsNotEmpty + " slots NOT empty");

                    SavedShulkerItems.put(customName, l_Items);
                }
            }
        }, 5000);
    }

    public void RenderLegacyShulkerPreview(EventRenderTooltip event) {
        ItemStack shulker = event.getItemStack();
        NBTTagCompound tagCompound = shulker.getTagCompound();
        if (tagCompound != null && tagCompound.hasKey("BlockEntityTag", 10)) {
            NBTTagCompound blockEntityTag = tagCompound.getCompoundTag("BlockEntityTag");
            if (blockEntityTag.hasKey("Items", 9)) {
                event.setCanceled(true);

                NonNullList<ItemStack> nonnulllist = NonNullList.withSize(27, ItemStack.EMPTY);
                ItemStackHelper.loadAllItems(blockEntityTag, nonnulllist); // load the itemstacks from the tag to
                // the list

                // store mouse/event coords
                int x = event.getX();
                int y = event.getY();

                // translate to mouse x, y
                GlStateManager.translate(x + 10, y - 5, 0);

                GlStateManager.disableLighting();
                GlStateManager.disableDepth();
                // background
                RenderUtils3D.drawRect(-3, -mc.fontRenderer.FONT_HEIGHT - 4, 9 * 16 + 3, 3 * 16 + 3, 0x99101010);
                RenderUtils3D.drawRect(-2, -mc.fontRenderer.FONT_HEIGHT - 3, 9 * 16 + 2, 3 * 16 + 2, 0xFF202020);
                RenderUtils3D.drawRect(0, 0, 9 * 16, 3 * 16, 0xFF101010);

                // text
                mc.fontRenderer.drawStringWithShadow(shulker.getDisplayName(), 0, -mc.fontRenderer.FONT_HEIGHT - 1,
                        0xFFFFFFFF);

                GlStateManager.enableDepth();
                mc.getRenderItem().zLevel = 150.0F;
                RenderHelper.enableGUIStandardItemLighting();

                // loop through items in shulker inventory
                for (int i = 0; i < nonnulllist.size(); i++) {
                    ItemStack itemStack = nonnulllist.get(i);
                    int offsetX = (i % 9) * 16;
                    int offsetY = (i / 9) * 16;
                    mc.getRenderItem().renderItemAndEffectIntoGUI(itemStack, offsetX, offsetY);
                    mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRenderer, itemStack, offsetX, offsetY, null);
                }

                RenderHelper.disableStandardItemLighting();
                mc.getRenderItem().zLevel = 0.0F;
                GlStateManager.enableLighting();

                // reverse the translate
                GlStateManager.translate(-(x + 10), -(y - 5), 0);
            }
        }

        if (this.middleClick.getValue()) {
            if (Mouse.isButtonDown(2)) {
                if (!this.clicked) {
                    final BlockShulkerBox shulkerBox = (BlockShulkerBox) Block.getBlockFromItem(shulker.getItem());
                    if (shulkerBox != null) {
                        final NBTTagCompound tag = shulker.getTagCompound();
                        if (tag != null && tag.hasKey("BlockEntityTag", 10)) {
                            final NBTTagCompound entityTag = tag.getCompoundTag("BlockEntityTag");

                            final TileEntityShulkerBox te = new TileEntityShulkerBox();
                            te.setWorld(mc.world);
                            te.readFromNBT(entityTag);
                            mc.displayGuiScreen(new GuiShulkerBox(mc.player.inventory, te));
                        }
                    }
                }
                this.clicked = true;
            } else {
                this.clicked = false;
            }
        }
    }

    public void Render2b2tShulkerPreview(EventRenderTooltip event) {
        if (!SavedShulkerItems.containsKey(event.getItemStack().getDisplayName()))
            return;

        final List<ItemStack> l_Items = SavedShulkerItems.get(event.getItemStack().getDisplayName());

        // store mouse/event coords
        int x = event.getX();
        int y = event.getY();

        // translate to mouse x, ye
        GlStateManager.translate(x + 10, y - 5, 0);

        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        // background
        //     RenderUtil.drawRect(-3, -mc.fontRenderer.FONT_HEIGHT - 4, 9 * 16 + 3, 3 * 16 + 3, 0x99101010);
        RenderUtils3D.drawRect(-2, -10 - 3, 9 * 16 + 2, 3 * 16 + 2, 0xFF202020);
        RenderUtils3D.drawRect(0, 0, 9 * 16, 3 * 16, 0xFF101010);

        // text
        mc.fontRenderer.drawStringWithShadow(event.getItemStack().getDisplayName(), 0, -12,
                0xFFFFFFFF);

        GlStateManager.enableDepth();
        mc.getRenderItem().zLevel = 150.0F;
        RenderHelper.enableGUIStandardItemLighting();

        for (int i = 0; i < l_Items.size(); ++i) {
            ItemStack itemStack = l_Items.get(i);
            if (itemStack == null)
                continue;

            // salhack.INSTANCE.logChat("Item: " + itemStack.getDisplayName());

            int offsetX = (i % 9) * 16;
            int offsetY = (i / 9) * 16;
            mc.getRenderItem().renderItemAndEffectIntoGUI(itemStack, offsetX, offsetY);
            mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRenderer, itemStack, offsetX, offsetY, null);
        }

        event.setCanceled(true);

        RenderHelper.disableStandardItemLighting();
        mc.getRenderItem().zLevel = 0.0F;
        GlStateManager.enableLighting();

        // reverse the translate
        GlStateManager.translate(-(x + 10), -(y - 5), 0);

        if (this.middleClick.getValue()) {
            if (Mouse.isButtonDown(2)) {
                if (!this.clicked) {
                    InventoryBasic l_Inventory = new InventoryBasic(event.getItemStack().getDisplayName(), true, 27);

                    for (ItemStack itemStack : l_Items) {
                        if (itemStack == null)
                            continue;

                        l_Inventory.addItem(itemStack);
                    }

                    mc.displayGuiScreen(new GuiChest(mc.player.inventory, l_Inventory));
                }
                this.clicked = true;
            } else {
                this.clicked = false;
            }
        }
    }

    public enum Modes {
        Normal, DropPacket, Inventory
    }
}

