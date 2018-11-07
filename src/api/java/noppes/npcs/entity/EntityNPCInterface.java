package noppes.npcs.entity;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public abstract class EntityNPCInterface extends EntityCreature implements ICommandSender, IRangedAttackMob, IAnimals {
    public ResourceLocation textureLocation = null;
    public EntityNPCInterface(World worldIn) {
        super(worldIn);
    }
}
