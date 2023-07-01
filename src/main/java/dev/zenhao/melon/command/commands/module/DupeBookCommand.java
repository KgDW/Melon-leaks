package dev.zenhao.melon.command.commands.module;

import dev.zenhao.melon.command.Command;
import dev.zenhao.melon.command.syntax.ChunkBuilder;
import dev.zenhao.melon.utils.Wrapper;
import dev.zenhao.melon.utils.chat.ChatUtil;
import io.netty.buffer.Unpooled;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemWritableBook;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;

import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DupeBookCommand
        extends Command {
    public DupeBookCommand() {
        super("dupebook", new ChunkBuilder().append("name").build());
        this.setDescription("Generates books used for chunk savestate dupe.");
    }

    @Override
    public void call(String[] args2) {
        ItemStack heldItem = Wrapper.getPlayer().inventory.getCurrentItem();
        if (heldItem.getItem() instanceof ItemWritableBook) {
            IntStream characterGenerator = new Random().ints(128, 1112063).map(i -> i < 55296 ? i : i + 2048);
            NBTTagList pages = new NBTTagList();
            String joinedPages = characterGenerator.limit(10500L).mapToObj(i -> String.valueOf((char) i)).collect(Collectors.joining());
            for (int page = 0; page < 50; ++page) {
                pages.appendTag(new NBTTagString(joinedPages.substring(page * 210, (page + 1) * 210)));
            }
            if (heldItem.hasTagCompound()) {
                assert (heldItem.getTagCompound() != null);
                heldItem.getTagCompound().setTag("pages", pages);
                heldItem.getTagCompound().setTag("title", new NBTTagString(""));
                heldItem.getTagCompound().setTag("author", new NBTTagString(Wrapper.getPlayer().getName()));
            } else {
                heldItem.setTagInfo("pages", pages);
                heldItem.setTagInfo("title", new NBTTagString(""));
                heldItem.setTagInfo("author", new NBTTagString(Wrapper.getPlayer().getName()));
            }
            PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
            buf.writeItemStack(heldItem);
            Wrapper.getPlayer().connection.sendPacket(new CPacketCustomPayload("MC|BEdit", buf));
            ChatUtil.NoSpam.sendWarnMessage("Dupe book generated.");
        } else {
            ChatUtil.NoSpam.sendErrorMessage("You must be holding a writable book.");
        }
    }
}

