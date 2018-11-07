package com.minelittlepony.render.ponies;

import com.minelittlepony.MineLittlePony;
import com.minelittlepony.model.ModelWrapper;
import com.minelittlepony.model.PMAPI;
import com.minelittlepony.pony.data.IPony;
import com.minelittlepony.pony.data.IPonyData;
import com.minelittlepony.pony.data.Pony;
import com.minelittlepony.render.RenderPony;
import com.minelittlepony.render.RenderPonyMob;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.voxelmodpack.hdskins.HDSkinManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.client.renderer.RenderNPCInterface;
import noppes.npcs.entity.EntityNpcPony;

import java.util.List;
import java.util.Map;

public class RenderPonyNpc<PONY extends EntityNpcPony> extends RenderNPCInterface<PONY> {

    private RenderPonyMob.Proxy<PONY> ponyRenderer;

    public RenderPonyNpc(RenderManager renderManager) throws NoSuchFieldException, IllegalAccessException {
        super(PMAPI.earthpony.getBody(), 0.5F);
        //noinspection JavaReflectionMemberAccess,unchecked
        this.ponyRenderer = new RenderPonyMob.Proxy<PONY>((List<LayerRenderer<PONY>>)RenderPonyNpc.class.getField("field_177097_h").get(this), renderManager, PMAPI.earthpony) {
            @Override
            public ResourceLocation getTexture(PONY entity) {
                renderPony = new RenderPonyBetter<>(this);
                return RenderPonyNpc.this.getEntityTexture(entity);
            }

            @Override
            public ModelWrapper getModelWrapper() {
                return renderPony.playerModel;
            }

            @Override
            public IPony getEntityPony(PONY entity) {
                return MineLittlePony.getInstance().getManager().getPony(getEntityTexture(entity));
            }
        };
    }

    class RenderPonyBetter<T extends EntityNpcPony> extends RenderPony<T> {

        private final RenderPonyMob.Proxy<T> renderer;

        public RenderPonyBetter(RenderPonyMob.Proxy<T> renderer) {
            super(renderer);
            this.renderer = renderer;
        }

        @Override
        public void updateModel(T entity) {
            IPony pony = renderer.getEntityPony(entity);
            ModelWrapper wrapper = pony.getRace(false).getModel().getModel(true);
            RenderPonyNpc.this.setMainModel(this.setPonyModel(wrapper));
            super.updateModel(entity);
        }

        @Override
        public IPony getPony(T entity) {
            updateModel(entity);
            return renderer.getEntityPony(entity);
        }
    }

    protected void setMainModel(ModelBase model) {
        this.mainModel = model;
    }

    @Override
    public void preRenderCallback(PONY entity, float partialTickTime) {
        ponyRenderer.preRenderCallback(entity, partialTickTime);
        super.preRenderCallback(entity, partialTickTime);
    }

    @Override
    protected boolean bindEntityTexture(PONY entity) {
        ResourceLocation resourcelocation = this.getEntityTexture(entity);

        if (resourcelocation == null) {
            return false;
        } else {
            this.bindTexture(resourcelocation);
            return true;
        }
    }

    private ResourceLocation getProfileTexture(GameProfile profile) {
        ResourceLocation skin = HDSkinManager.INSTANCE.getTextures(profile).get(MinecraftProfileTexture.Type.SKIN);
        if (skin != null && Pony.getBufferedImage(skin) != null) {
            return skin;
        }
        Minecraft minecraft = Minecraft.getMinecraft();
        Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = minecraft.getSkinManager().loadSkinFromCache(profile);

        if (map.containsKey(MinecraftProfileTexture.Type.SKIN)) {
            ResourceLocation loc = minecraft.getSkinManager().loadSkin(map.get(MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN);
            if (Pony.getBufferedImage(loc) != null) {
                return loc;
            }
        }
        return DefaultPlayerSkin.getDefaultSkin(EntityPlayer.getUUID(profile));
    }

    @Override
    public ResourceLocation getEntityTexture(PONY npc) {
        if (npc.textureLocation == null || npc.display.skinType == 1 && npc.display.playerProfile != null) {
            npc.textureLocation = getProfileTexture(npc.display.playerProfile);
        } else {
            npc.textureLocation = super.getEntityTexture(npc);
        }
        return npc.textureLocation;
    }

    @Override
    public void doRender(PONY entity, double x, double y, double z, float entityYaw, float partialTicks) {
        IPony pony = this.ponyRenderer.getEntityPony(entity);
        IPonyData ponydata = pony.getMetadata();

        ModelWrapper wrapper = pony.getRace(false).getModel().getModel(true);
        wrapper.apply(ponydata);
        mainModel = ponyRenderer.getInternalRenderer().setPonyModel(wrapper);
        ponyRenderer.getInternalRenderer().updateModel(entity);
        try {
            super.doRender(entity, x, y, z, entityYaw, partialTicks);
        } finally {
            mainModel = null;
        }
    }
}
