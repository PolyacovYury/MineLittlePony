package com.minelittlepony.pony.data;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.minelittlepony.MineLittlePony;
import com.minelittlepony.ducks.IRenderPony;
import com.voxelmodpack.hdskins.resources.texture.IBufferedTexture;

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
public class Pony implements IPony {

    private static final AtomicInteger ponyCount = new AtomicInteger();

    private final int ponyId = ponyCount.getAndIncrement();

    private final ResourceLocation texture;
    private final IPonyData metadata;

    public Pony(ResourceLocation resource) {
        texture = resource;
        metadata = checkSkin(texture);
    }

    private IPonyData checkSkin(ResourceLocation resource) {
        IPonyData data = checkPonyMeta(resource);
        if (data != null) {
            return data;
        }

        BufferedImage skinImage = Preconditions.checkNotNull(getBufferedImage(resource), "bufferedImage: " + resource);
        return this.checkSkin(skinImage);
    }

    @Nullable
    private IPonyData checkPonyMeta(ResourceLocation resource) {
        try {
            IResource res = Minecraft.getMinecraft().getResourceManager().getResource(resource);
            if (res.hasMetadata()) {
                PonyData data = res.getMetadata(PonyDataSerialiser.NAME);
                if (data != null) {
                    return data;
                }
            }
        } catch (FileNotFoundException e) {
            // Ignore uploaded texture
        } catch (IOException e) {
            MineLittlePony.logger.warn("Unable to read {} metadata", resource, e);
        }
        return null;
    }

    @Nullable
    public static BufferedImage getBufferedImage(@Nonnull ResourceLocation resource) {
        try {
            IResource skin = Minecraft.getMinecraft().getResourceManager().getResource(resource);
            BufferedImage skinImage = TextureUtil.readBufferedImage(skin.getInputStream());
            MineLittlePony.logger.debug("Obtained skin from resource location {}", resource);

            return skinImage;
        } catch (IOException ignored) {
        }

        ITextureObject texture = Minecraft.getMinecraft().getTextureManager().getTexture(resource);

        if (texture instanceof IBufferedTexture) {
            return ((IBufferedTexture) texture).getBufferedImage();
        }

        return null;
    }

    private IPonyData checkSkin(BufferedImage bufferedimage) {
        MineLittlePony.logger.debug("\tStart skin check for pony #{} with image {}.", ponyId, bufferedimage);
        return PonyData.parse(bufferedimage);
    }

    @Override
    public boolean isFlying(EntityLivingBase entity) {
        return !(entity.onGround || entity.isRiding() || entity.isOnLadder() || entity.isInWater());
    }

    @Override
    public boolean isSwimming(EntityLivingBase entity) {
        return isFullySubmerged(entity) && !(entity.onGround || entity.isOnLadder());
    }

    @Override
    public boolean isFullySubmerged(EntityLivingBase entity) {
        return entity.isInWater()
                && entity.getEntityWorld().getBlockState(new BlockPos(getVisualEyePosition(entity))).getMaterial() == Material.WATER;
    }

    protected Vec3d getVisualEyePosition(EntityLivingBase entity) {
        PonySize size = entity.isChild() ? PonySize.FOAL : metadata.getSize();

        return new Vec3d(entity.posX, entity.posY + (double) entity.getEyeHeight() * size.getScaleFactor(), entity.posZ);
    }

    @Override
    public boolean isWearingHeadgear(EntityLivingBase entity) {
        ItemStack stack = entity.getItemStackFromSlot(EntityEquipmentSlot.HEAD);

        if (stack.isEmpty()) {
            return false;
        }

        Item item = stack.getItem();

        return !(item instanceof ItemArmor) || ((ItemArmor) item).getEquipmentSlot() != EntityEquipmentSlot.HEAD;
    }

    @Override
    public PonyRace getRace(boolean ignorePony) {
        return metadata.getRace().getEffectiveRace(ignorePony);
    }

    @Override
    public ResourceLocation getTexture() {
        return texture;
    }

    @Override
    public IPonyData getMetadata() {
        return metadata;
    }

    @Override
    public boolean isRidingInteractive(EntityLivingBase entity) {
        return MineLittlePony.getInstance().getRenderManager().getPonyRenderer(entity.getRidingEntity()) != null;
    }

    @Override
    public IPony getMountedPony(EntityLivingBase entity) {
        Entity mount = entity.getRidingEntity();

        IRenderPony<EntityLivingBase> render = MineLittlePony.getInstance().getRenderManager().getPonyRenderer(mount);

        return render == null ? null : render.getEntityPony((EntityLivingBase)mount);
    }

    @Override
    public Vec3d getAbsoluteRidingOffset(EntityLivingBase entity) {
        IPony ridingPony = getMountedPony(entity);

        if (ridingPony != null) {
            EntityLivingBase ridee = (EntityLivingBase)entity.getRidingEntity();

            Vec3d offset = ridingPony.getMetadata().getSize().getTranformation().getRiderOffset();
            return ridingPony.getAbsoluteRidingOffset(ridee).add(-offset.x / 4, offset.y / 5, -offset.z / 4);
        }

        return entity.getPositionVector();
    }

    @Override
    public AxisAlignedBB getComputedBoundingBox(EntityLivingBase entity) {
        float scale = getMetadata().getSize().getScaleFactor();

        Vec3d pos = getAbsoluteRidingOffset(entity);

        return new AxisAlignedBB(
                - entity.width / 2, (entity.height * scale), -entity.width / 2,
                  entity.width / 2, 0,                        entity.width / 2).offset(pos);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("texture", texture)
                .add("metadata", metadata)
                .toString();
    }
}
