package BotBrains.Goals;

import BotBrains.Action;
import BotBrains.Actions.BuildAction;
import BotBrains.DecisionMaker;
import BotBrains.Goal;
import BotBrains.Util;
import com.springrts.ai.oo.clb.OOAICallback;
import com.springrts.ai.oo.clb.Resource;
import com.springrts.ai.oo.clb.UnitDef;

public class MakeSolarGoal extends Goal {

    Resource res = null;

    @Override
    protected float recalculateValue() {
        //this needs to determine how important this goal is

        //take average of current and 10 frame estimate
        OOAICallback clb = DecisionMaker.get().getClb();
        res = clb.getResourceByName("Energy");
        float current = clb.getEconomy().getCurrent(res);
        float delta = clb.getEconomy().getIncome(res) - clb.getEconomy().getUsage(res);

        float average = current + 10 * delta;
        float storage = clb.getEconomy().getStorage(res);

        float value = 1 - Math.min(Math.max(average / storage, 0), 1);

        return value;

    }

    @Override
    protected String getName() {
        return "MakeSolar";
    }

    @Override
    public float getGoalChange(Action action) {

        OOAICallback clb = DecisionMaker.get().getClb();
        res = clb.getResourceByName("Energy");

        if (action instanceof BuildAction) {
            //going to build, check out how much metal this will create

            //cost to build it
            UnitDef unitDef = action.def_buildeeUnit;

            if (unitDef.getSpeed() == 0) {

                //cost/gain to run it
                float make = unitDef.getExtractsResource(res) + unitDef.getResourceMake(res) + unitDef.getMakesResource(res) - unitDef.getUpkeep(res) - Util.getSolarSuck(unitDef);

                return make;
            }
        }
        //no build = no care
        return 0;


    }
}