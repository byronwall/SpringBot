package BotBrains.Goals;

import BotBrains.Action;
import BotBrains.Actions.BuildAction;
import BotBrains.DecisionMaker;
import BotBrains.Goal;
import BotBrains.Util;
import com.springrts.ai.oo.clb.OOAICallback;
import com.springrts.ai.oo.clb.Resource;
import com.springrts.ai.oo.clb.UnitDef;

public class DoNotBuildMore extends Goal {

    Resource res = null;

    @Override
    protected float recalculateValue() {
        //this needs to determine how important this goal is

        //take average of current and 10 frame estimate
        OOAICallback clb = DecisionMaker.get().getClb();

        float value = 1;
        for (Resource res : clb.getResources()) {


            float current = clb.getEconomy().getCurrent(res);
            float delta = clb.getEconomy().getIncome(res) - clb.getEconomy().getUsage(res);

            float average = current + 10 * delta;
            float storage = clb.getEconomy().getStorage(res);

            value *= 1 - Util.clamp(average / storage, 0, 1);
        }

        return value;

    }

    @Override
    protected String getName() {
        return "DoNotBuildMore";
    }

    @Override
    public float getGoalChange(Action action) {

        OOAICallback clb = DecisionMaker.get().getClb();

        //TODO verify this is best way to do this
        if (action instanceof BuildAction) {
            //need to check each resource and combine "build-cost ratios" ==> cost/storage
            UnitDef unitDef = action.def_buildeeUnit;

            float value = 1;

            for (Resource res : clb.getResources()) {
                float cost = unitDef.getCost(res);
                float storage = clb.getEconomy().getStorage(res);

                value *= Util.clamp(1 - cost / storage, 0, 1);
            }

            return value;

        } else {
            //no build = no care
            return 1;
        }

    }
}