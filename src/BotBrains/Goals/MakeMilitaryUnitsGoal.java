package BotBrains.Goals;

import BotBrains.Action;
import BotBrains.Actions.BuildAction;
import BotBrains.DecisionMaker;
import BotBrains.Goal;
import BotBrains.Util;
import com.springrts.ai.oo.clb.Unit;
import com.springrts.ai.oo.clb.UnitDef;

public class MakeMilitaryUnitsGoal extends Goal {

    @Override
    protected float recalculateValue() {
        //this needs to determine how important this goal is

        //assume that the military needs to be some percent of the total units


        int units_total = 0;
        int units_military = 0;
        for (Unit unit : DecisionMaker.get().getClb().getTeamUnits()) {
            units_total++;
            UnitDef def = unit.getDef();
            if (!def.isBuilder() && def.getWeaponMounts().size() > 0) {
                units_military++;
            }
        }

        //don't start with military
        float value = 0;

        if (units_total > 5) {
            value = 1 - 2.0f * units_military / units_total;
        }

        return value;

    }

    @Override
    protected String getName() {
        return "MakeMilitary";
    }

    @Override
    public float getGoalChange(Action action) {

        if (action instanceof BuildAction) {
            //going to build... how good is this unit
            //this just picks the one with the biggest guns
            //TODO: somehow factor in unit speed, range, cost, etc.

            return Util.getTotalDamage(action.def_buildeeUnit);

        } else {
            //no build = no care
            return 0;
        }

    }

}