package noppes.npcs.client.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.entity.EntityNPCInterface;

public abstract class RenderNPCInterface<T extends EntityNPCInterface> extends RenderLiving<T> {
    public RenderNPCInterface(ModelBase model, float f){
        super(Minecraft.getMinecraft().getRenderManager(), model, f);
    }

    @Override
    public ResourceLocation getEntityTexture(T npc) {
        return new ResourceLocation("");
    }
}
