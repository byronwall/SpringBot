package BotBrains.Goals;

import BotBrains.Action;
import BotBrains.Goal;
import BotBrains.Util;
import com.springrts.ai.oo.clb.Resource;

public class RandomBuildGoal extends Goal {

    @Override
    protected float recalculateValue() {
        //this needs to determine how important this goal is

        return 0.5f;

    }

    @Override
    public float getGoalChange(Action action) {

        return Util.RAND.nextFloat();

    }
}