package BotBrains.Actions;

import BotBrains.Action;
import BotBrains.Util;
import com.springrts.ai.oo.AIFloat3;
import com.springrts.ai.oo.clb.Unit;

/**
 * Created by byronandanne on 11/30/2014.
 */
public class RepairAction extends Action {

    private RepairAction() {
    }

    public static RepairAction createAction(Unit unit) {


        //only applies to builders
        if (!unit.getDef().isBuilder() || !unit.getDef().isAbleToRepair()) {
            return null;
        }

        RepairAction action = new RepairAction();

        action.def_builderUnit = unit;
        //TODO fix this reference

        //iterate through nearby areas and pick the "most" dangerous

        action.loc_action = Util.getRandomNearbyPosition(unit.getPos(), 500);

        return action;
    }

    @Override
    public void processAction() {
//set to roam.... hopefully he attacks now.

        //TODO determine a better way to do this.  need to actually repair and get back to normal

        //this.def_builderUnit.patrolTo(this.loc_action, (short) 0, 0);

    }

    @Override
    public AIFloat3 findLocationForAction() {

        return this.loc_action;
    }

    @Override
    public String toString() {

        return String.format("REPAIR action, unit: %s, loc: %s", def_builderUnit.getDef().getName(), loc_action);

    }

}
