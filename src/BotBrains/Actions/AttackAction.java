package BotBrains.Actions;

import BotBrains.Action;
import BotBrains.DecisionMaker;
import BotBrains.SpringBot;
import BotBrains.Util;
import com.springrts.ai.oo.AIFloat3;
import com.springrts.ai.oo.clb.Unit;

/**
 * Created by byronandanne on 11/30/2014.
 */
public class AttackAction extends Action {

    private AttackAction() {
    }

    public static AttackAction createAction(Unit unit) {


        if (unit.getDef().getWeaponMounts().size() == 0 ||
                !unit.getDef().isAbleToMove() ||
                unit.getDef().isCommander() ||
                unit.getDef().getName().equals("armcom") ||
                unit.getDef().getName().equals("corcom")) {
            return null;
        }

        AttackAction action = new AttackAction();

        action.def_builderUnit = unit;
        //TODO fix this reference
        action.loc_action = DecisionMaker.get().ThreatMap.getHighestValue();

        SpringBot.write("attack action added:" + action);

        return action;
    }

    @Override
    public void processAction() {
//set to roam.... hopefully he attacks now.
        this.def_builderUnit.setMoveState(Util.MOVE_STATE_ROAM, (short) 0, 0);
        this.def_builderUnit.fight(this.loc_action, (short) 0, 0);


    }

    @Override
    public AIFloat3 findLocationForAction() {

        return this.loc_action;
    }

    @Override
    public String toString() {

        return String.format("ATTACK action, unit: %s, loc: %s", def_builderUnit.getDef().getName(), loc_action);

    }

}
