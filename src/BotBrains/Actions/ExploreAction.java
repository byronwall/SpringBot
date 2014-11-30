package BotBrains.Actions;

import BotBrains.Action;
import BotBrains.DecisionMaker;
import BotBrains.SpringBot;
import BotBrains.Util;
import com.springrts.ai.oo.AIFloat3;
import com.springrts.ai.oo.clb.OOAICallback;
import com.springrts.ai.oo.clb.Unit;

/**
 * Created by byronandanne on 11/30/2014.
 */
public class ExploreAction extends Action {


    public static ExploreAction createAction(Unit unit) {


        if (unit.getDef().getSpeed() == 0) {
            return null;
        }

        ExploreAction action = new ExploreAction();

        action.def_builderUnit = unit;
        OOAICallback clb = DecisionMaker.get().getClb();

        //allow faster units to travel farther
        float EXPLORER_DIST = unit.getDef().getSpeed() * 20;

        float z_delta = Util.RAND.nextFloat() * EXPLORER_DIST - EXPLORER_DIST / 2;
        float x_delta = Util.RAND.nextFloat() * EXPLORER_DIST - EXPLORER_DIST / 2;

        AIFloat3 pos = unit.getPos();
        pos.x += x_delta;
        pos.z += z_delta;

        //clamp the values
        pos.x = Util.clamp(pos.x, 0, clb.getMap().getWidth() * 8);
        pos.z = Util.clamp(pos.z, 0, clb.getMap().getHeight() * 8);


        AIFloat3 pos_map = DecisionMaker.get().VisitedMap.getLowestValue();

        //choose between the closer location
        if (Util.calcDist(unit.getPos(), pos) < Util.calcDist(unit.getPos(), pos_map)) {
            action.loc_action = pos;
        } else {
            action.loc_action = pos_map;
        }

        SpringBot.write("EXPLORE action added:" + action);

        return action;
    }

    @Override
    public void processAction() {
        this.def_builderUnit.setMoveState(Util.MOVE_STATE_ROAM, (short) 0, 0);
        this.def_builderUnit.moveTo(this.loc_action, (short) 0, 0);
    }

    @Override
    public AIFloat3 findLocationForAction() {

        return this.loc_action;
    }

    @Override
    public String toString() {

        return String.format("EXPLORE action: unit: %s, loc: %s", def_builderUnit.getDef().getName(), loc_action);


    }
}
