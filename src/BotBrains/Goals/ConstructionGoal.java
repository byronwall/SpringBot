package BotBrains.Goals;

import BotBrains.Action;
import BotBrains.Actions.BuildAction;
import BotBrains.Actions.RepairAction;
import BotBrains.DecisionMaker;
import BotBrains.Goal;
import BotBrains.Util;
import com.springrts.ai.oo.clb.Unit;
import com.springrts.ai.oo.clb.UnitDef;

//this goal is to ensure builders are created and that they are put to use

public class ConstructionGoal extends Goal {

    float target_ratio = 0.2f;

    @Override
    protected float recalculateValue() {
        //this needs to determine how important this goal is

        //count number of buildings that are factories
        //has a build list and cannot move
        int units_total = 0;
        int units_build = 0;
        for (Unit unit : DecisionMaker.get().getClb().getTeamUnits()) {
            units_total++;
            UnitDef def = unit.getDef();
            if (def.isBuilder()) {
                units_build++;
            }
        }

        //don't start with military
        float value = 0;

        //idea here is that the error will be at most target_ratio
        //divide by target ratio to get a number from 0 to 1;
        if (units_total > 5) {
            value = Util.clamp((target_ratio - 1.0f * units_build / units_total) / target_ratio, 0, 1);
        }

        return value;

    }

    @Override
    protected String getName() {
        return "Construction";
    }

    @Override
    public float getGoalChange(Action action) {

        if (action instanceof BuildAction) {
            //going to build... how good is this unit

            //this will be a 0 or 1 choice... is it a factory?

            UnitDef def = action.def_buildeeUnit;
            if (def.isBuilder()) {
                return def.getBuildSpeed();
            }


        } else if (action instanceof RepairAction) {
            return 1;
        }

        return 0;
    }
}