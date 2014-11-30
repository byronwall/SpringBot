package BotBrains.Goals;

import BotBrains.Action;
import BotBrains.Actions.BuildAction;
import BotBrains.DecisionMaker;
import BotBrains.Goal;
import com.springrts.ai.oo.clb.Unit;

public class ForceSingleUnit extends Goal {

    private String unit_name = "armlab";

    @Override
    protected float recalculateValue() {
        //this needs to determine how important this goal is

        //assume that the military needs to be some percent of the total units

        for (Unit unit : DecisionMaker.get().getClb().getTeamUnits()) {
            if (unit.getDef().getName().equals(unit_name)) {

                return 0;
            }
        }


        return 100;

    }

    @Override
    public float getGoalChange(Action action) {

        if (action instanceof BuildAction) {

            if (action.def_buildeeUnit.getName().equals(unit_name)) {

                return 100;
            }

        }

        return 0;

    }
}