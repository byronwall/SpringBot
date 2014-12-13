package BotBrains.Actions;

import BotBrains.Action;
import BotBrains.DecisionMaker;
import BotBrains.SpringBot;
import BotBrains.Util;
import com.springrts.ai.oo.AIFloat3;
import com.springrts.ai.oo.clb.OOAICallback;
import com.springrts.ai.oo.clb.Unit;

import java.util.HashMap;

/**
 * Created by byronandanne on 11/30/2014.
 */
public class ExploreAction extends Action {


    public static ExploreAction createAction(Unit unit) {


        if (unit.getDef().getSpeed() == 0 ||
                unit.getDef().isBuilder() ||
                unit.getDef().getName().equals("armcom") ||
                unit.getDef().getName().equals("corcom")) {
            return null;
        }

        ExploreAction action = new ExploreAction();

        action.def_builderUnit = unit;
        OOAICallback clb = DecisionMaker.get().getClb();

        //allow faster units to travel farther

        HashMap<AIFloat3, Float> areas = DecisionMaker.get().VisitedMap.nearbyValues(unit.getPos(), false);

        float min_value = Float.MAX_VALUE;
        for (AIFloat3 pos : areas.keySet()) {
            //SpringBot.write("explore pos: " + pos + ", value: " + areas.get(pos));
            if (areas.get(pos) < min_value) {
                min_value = areas.get(pos);
                action.loc_action = pos;
            }
        }

        float EXPLORER_DIST = unit.getDef().getSpeed() * 20;

        float z_delta = Util.RAND.nextFloat() * EXPLORER_DIST - EXPLORER_DIST / 2;
        float x_delta = Util.RAND.nextFloat() * EXPLORER_DIST - EXPLORER_DIST / 2;

        action.loc_action.x += x_delta;
        action.loc_action.z += z_delta;

        //clamp the values
        action.loc_action.x = Util.clamp(action.loc_action.x, 0, clb.getMap().getWidth() * 8);
        action.loc_action.z = Util.clamp(action.loc_action.z, 0, clb.getMap().getHeight() * 8);

        SpringBot.write("EXPLORE action added:" + action);

        return action;
    }

    @Override
    public void processAction() {
        this.def_builderUnit.setMoveState(Util.MOVE_STATE_ROAM, (short) 0, 0);
        this.def_builderUnit.fight(this.loc_action, (short) 0, 0);
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
