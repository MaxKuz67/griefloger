package com.daqem.grieflogger.model;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.EncoderException;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class SimpleItemStack {

    private final Item item;
    private int count;
    private final DataComponentPatch tag;

    public SimpleItemStack(ItemStack itemStack) {
        this(itemStack.getItem(), itemStack.getCount(), itemStack.getComponentsPatch());
    }

    public SimpleItemStack(ResourceLocation itemLocation, int count, DataComponentPatch tag) {
        this.item = BuiltInRegistries.ITEM.get(itemLocation);
        this.count = count;
        this.tag = tag;
    }

    public SimpleItemStack(Item item, int count, DataComponentPatch tag) {
        this.item = item;
        this.count = count;
        this.tag = tag;
    }

    @Override
    public boolean equals(Object o) {
        //DOES NOT CHECK COUNT
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleItemStack that = (SimpleItemStack) o;
        return Objects.equals(item, that.item) && Objects.equals(tag, that.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(item, count, tag);
    }

    public Item getItem() {
        return item;
    }

    public int getCount() {
        return count;
    }

    public DataComponentPatch getTag() {
        return tag;
    }

    public boolean hasTag() {
        return tag != null;
    }

    public boolean hasNoTag() {
        return tag == null;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void addCount(int count) {
        this.count += count;
    }

    public byte @Nullable [] getTagBytes(Level level) {
        // если патча нет — как и раньше, ничего не пишем в БД
        if (tag == null  || tag.isEmpty()) {
            return null;
        }
        try {
            RegistryFriendlyByteBuf buf =
                    new RegistryFriendlyByteBuf(Unpooled.buffer(), level.registryAccess());
            DataComponentPatch.STREAM_CODEC.encode(buf, tag); // здесь и падало
            byte[] temp = new byte[buf.readableBytes()];
            buf.readBytes(temp);
            return temp;
        } catch (EncoderException | IllegalArgumentException e) {
            // внутри патча встретился пустой ItemStack (или другой невалидный компонент) — пропускаем
            return null; // возвращаем null, чтобы логика insert в БД не менялась
        }
    }

    public ItemStack toItemStack() {
        ItemStack itemStack = new ItemStack(item, count);
        itemStack.applyComponents(tag);
        return itemStack;
    }

    public boolean isEmpty() {
        return item.equals(ItemStack.EMPTY.getItem()) || count == 0;
    }
}
