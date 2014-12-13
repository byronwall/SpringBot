package BotBrains.Goals;

import BotBrains.*;
import BotBrains.Actions.ExploreAction;

public class ExpandEmpireGoal extends Goal {

    float target_ratio = 0.5f;

    @Override
    protected float recalculateValue() {
        //this needs to determine how important this goal is

        //count number of buildings that are factories
        //has a build list and cannot move

        float frac = DecisionMaker.get().VisitedMap.fractionAbove(0);
        float value = Util.clamp((target_ratio - frac) / target_ratio, 0, 1);

        SpringBot.write("expand empire goal: " + value + ", w/ " + "frac :" + frac);

        return value;

    }

    @Override
    public float getGoalChange(Action action) {

        if (action instanceof ExploreAction) {
            //going to build... how good is this unit

            //this will be a 0 or 1 choice... is it a factory?

            return action.def_builderUnit.getSpeed();
        }

        return 0;
    }

    //TODO: create a method for determining if it is a factory
}