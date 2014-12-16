package BotBrains.Goals;

import BotBrains.Action;
import BotBrains.Goal;
import BotBrains.Util;

public class RandomBuildGoal extends Goal {

    @Override
    protected float recalculateValue() {
        //this needs to determine how important this goal is

        //TODO make this a parmater for the model
        return 1.0f;

    }

    @Override
    protected String getName() {
        return "Random";
    }

    @Override
    public float getGoalChange(Action action) {

        return Util.RAND.nextFloat();

    }
}