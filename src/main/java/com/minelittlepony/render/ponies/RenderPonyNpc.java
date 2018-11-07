package com.minelittlepony.render.ponies;

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
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.entity.EntityNpcPony;

import java.util.Map;

public class RenderPonyNpc<PONY extends EntityNpcPony> extends RenderPonyMob<PONY> {

    public RenderPonyNpc(RenderManager renderManager) {
        super(renderManager, PMAPI.earthpony);//earthpony, only so MineLP code doesn't crash. rather silly idea to separate all the models this much, but oh well.
        this.renderPony = new RenderPonyBetter(this);
    }

    protected void setMainModel(ModelBase model) {
        this.mainModel = model;
    }

    @Override
    public void preRenderCallback(PONY entity, float partialTickTime) {
        super.preRenderCallback(entity, partialTickTime);
    }

    @Override
    protected boolean bindEntityTexture(PONY entity) {
        ResourceLocation resourcelocation = this.getTexture(entity);

        if (resourcelocation == null) {
            return false;
        } else {
            this.bindTexture(resourcelocation);
            return true;
        }
    }

    private  ResourceLocation getProfileTexture(GameProfile profile) {
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
    public ResourceLocation getTexture(PONY npc) {
        if(npc.textureLocation == null){
            if(npc.display.skinType == 1 && npc.display.playerProfile != null){
                npc.textureLocation = getProfileTexture(npc.display.playerProfile);
            } else {
                npc.textureLocation = super.getEntityTexture(npc);
            }
        }
        return npc.textureLocation;
    }

    @Override
    public void doRender(PONY entity, double x, double y, double z, float entityYaw, float partialTicks) {
        IPony pony = this.getEntityPony(entity);
        IPonyData ponydata = pony.getMetadata();

        ModelWrapper wrapper = pony.getRace(false).getModel().getModel(true);
        mainModel = renderPony.setPonyModel(wrapper);
        renderPony.updateModel(entity);
        wrapper.apply(ponydata);
        try {
            super.doRender(entity, x, y, z, entityYaw, partialTicks);
        } finally {
            mainModel = null;
        }
    }

    @Override
    public ModelWrapper getModelWrapper() {
        return renderPony.playerModel;
    }

    @Override
    public IPony getEntityPony(PONY entity) {
        //return MineLittlePony.getInstance().getManager().getPony(getEntityTexture(entity), false);
        return (IPony) entity;
    }

    public class RenderPonyBetter<T extends EntityNpcPony> extends RenderPony<T> {

        private final RenderPonyNpc renderer;

        public RenderPonyBetter(RenderPonyNpc renderer) {
            super(renderer);
            this.renderer = renderer;
        }

        @Override
        public void updateModel(T entity) {
            //super.updateModel(entity);
            //pony = renderer.getEntityPony(entity);
            IPony pony = (IPony) entity;
            ModelWrapper wrapper = pony.getRace(false).getModel().getModel(true);
            renderer.setMainModel(this.setPonyModel(wrapper));
            //playerModel.apply(entity.getMyLogic(PonyLogicReasoning.VISUAL).getPonyTypeInfo());
            super.updateModel(entity);
        }

        @Override
        public IPony getPony(T entity) {
            updateModel(entity);
            return (IPony) entity;
        }
    }
}
