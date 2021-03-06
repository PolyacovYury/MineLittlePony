package com.minelittlepony.model.player;

import com.minelittlepony.model.components.BatWings;
import com.minelittlepony.pony.data.PonyWearable;
import com.minelittlepony.render.model.PonyRenderer;

public class ModelBatpony extends ModelPegasus {

    public ModelBatpony(boolean smallArms) {
        super(smallArms);
    }

    @Override
    protected void initWings(float yOffset, float stretch) {
        wings = new BatWings<>(this, yOffset, stretch);
    }

    @Override
    protected void initEars(PonyRenderer head, float yOffset, float stretch) {
        head.child()
            .tex(12, 16).box(-4, -6, 1, 2, 2, 2, stretch)  // right ear
              .tex(0, 3).box(-3.5F, -6.49F, 1.001F, 1, 1, 1, stretch)
              .tex(0, 5).box(-2.998F, -6.49F, 2.001F, 1, 1, 1, stretch);

        head.child().flip()
            .tex(12, 16).box( 2, -6, 1, 2, 2, 2, stretch)  // left ear
              .tex(0, 3).box( 2.5F, -6.49F, 1.001F, 1, 1, 1, stretch)
              .tex(0, 5).box( 1.998F, -6.49F, 2.001F, 1, 1, 1, stretch);
    }

    @Override
    public boolean isWearing(PonyWearable wearable) {
        if (wearable == PonyWearable.SADDLE_BAGS) {
            return false;
        }

        return super.isWearing(wearable);
    }
}
