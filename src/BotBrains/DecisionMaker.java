package BotBrains;

import BotBrains.Actions.AttackAction;
import BotBrains.Actions.BuildAction;
import BotBrains.Actions.NothingAction;
import BotBrains.Actions.RepairAction;
import BotBrains.Goals.*;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.springrts.ai.oo.AIFloat3;
import com.springrts.ai.oo.clb.OOAICallback;
import com.springrts.ai.oo.clb.Unit;
import com.springrts.ai.oo.clb.UnitDef;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by byronandanne on 11/26/2014.
 */
public class DecisionMaker {
    public static final String TABLE = "action_log";
    public static int id = 0;
    private static DecisionMaker maker = null;
    public DataMap ThreatMap;
    public DataMap VisitedMap;
    public DataMap BuildingMap;
    List<Goal> goals = new ArrayList<Goal>();
    OOAICallback clb = null;
    UnitDef unit_first = null;
    HashMap<String, Boolean> skipUnitType = new HashMap<>();

    private DecisionMaker() {
        id = Util.RAND.nextInt(123456);
    }

    public static DecisionMaker get() {
        if (maker == null) {
            maker = new DecisionMaker();
        }

        return maker;
    }

    public UnitDef get_firstUnit() {
        return unit_first;
    }

    public void setCallback(OOAICallback callback) {
        clb = callback;
    }

    public OOAICallback getClb() {
        return clb;
    }

    public Action chooseAction(List<Action> actions) {
        Table<Action, Goal, Float> values = HashBasedTable.create();

        //load up table with values

        //SpringBot.write("ready to choose");
        for (Action action : actions) {
            for (Goal goal : goals) {
                //SpringBot.write("processing goal: " + goal);
                values.put(action, goal, goal.getGoalChange(action));
            }

        }

        //normal so that all goals have 1 max
        for (Goal g : goals) {
            float max = Collections.max(values.column(g).values());

            //update for each action
            if (max != 0) {
                for (Action a : actions) {

                    values.put(a, g, values.get(a, g) / max);

                }
            }
        }

        //find the best action for that goal
        Action top_action = null;
        float max_utility = -Float.MAX_VALUE;
        for (Action a : actions) {


            //update for each action
            float utility = 0;
            for (Goal g : goals) {
                utility += values.get(a, g) * g.getGoalValue();
            }

            //TODO reenable this if needed
            //DatabaseMaster.get().addFrameData(TABLE, "action: " + a + ", util: " + utility, SpringBot.FRAME);

            if (utility > max_utility) {
                top_action = a;
                max_utility = utility;

                //SpringBot.write("new best action: " + a.toString() + ", " + utility);
            }
        }

        //now we have modified action scores... need to sum product with goal scores to pick

        return top_action;
    }

    public void ProcessUnit(Unit unit) {

        if (unit == null) {
            return;
        }

        //init the first unit
        if (unit_first == null) {
            unit_first = unit.getDef();
            VisitedMap.addToMap(unit.getPos(), 20);
        }

        //this will skip "dead" units for now
        if (unit == null || unit.getDef() == null || unit.getDef().getName() == null) {
            DatabaseMaster.get().quickLog("Null issue on ProcessUnit" + unit.getDef());
            return;
        }
        String unitName = unit.getDef().getName();
        if (skipUnitType.containsKey(unitName)) {
            if (skipUnitType.get(unitName)) {
                return;
            }
        }

        List<Action> actions = new ArrayList<Action>();

        //add in all the things to do here
        //SpringBot.write("creating build actions for :" + unit.getDef().getName());
        List<Action> buildActions = BuildAction.createAllActions(unit);
        if (buildActions != null) {
            actions.addAll(buildActions);
        }
        //SpringBot.write("creating attack actions for :" + unit.getDef().getName());
        AttackAction attackAction = AttackAction.createAction(unit);
        if (attackAction != null) {
            actions.add(attackAction);
        }

        /*
        ExploreAction exploreAction = ExploreAction.createAction(unit);
        if (exploreAction != null) {
            actions.add(exploreAction);
        }
        */

        RepairAction repairAction = RepairAction.createAction(unit);
        if (repairAction != null) {
            actions.add(repairAction);
        }

        if (actions.size() > 0) {
            actions.add(new NothingAction());
        }

        //size ==1 => only the do nothing option
        if (actions.size() > 0) {
            DecideAndExecute(actions);
        } else {
            skipUnitType.put(unitName, true);
        }

    }

    public void resetGoals() {
        for (Goal goal : goals) {
            goal.resetValue();
        }
    }

    public void InitializeGoals() {
        //this is currently where the "strategy" is loaded up
        goals.add(new MakeMetalGoal());
        goals.add(new MakeSolarGoal());
        goals.add(new StoreSolarGoal());
        goals.add(new StoreMetalGoal());
        goals.add(new RandomBuildGoal());
        goals.add(new MakeMilitaryUnitsGoal());
        //goals.add(new ForceSingleUnit());
        goals.add(new DoNotBuildMore());
        goals.add(new BuildFactoriesGoal());
        goals.add(new PursueMoreTechGoal());
        goals.add(new ExpandEmpireGoal());
        goals.add(new ConstructionGoal());
        goals.add(new BuildDefenseGoal());
    }

    public void DecideAndExecute(List<Action> actions) {
        //need to choose the best action

        //break if no action
        if (actions.size() == 0) return;

        Action best_action = null;
        AIFloat3 buildSite = null;

        int counter = 0;
        while (true) {
            counter++;

            best_action = chooseAction(actions);

            if (best_action == null) {
                break;
            }

            DatabaseMaster.get().addFrameData(TABLE, "best action: " + best_action.toString() + ", counter: " + counter, SpringBot.FRAME);

            //need to pick build location and check if it can be done

            buildSite = best_action.findLocationForAction();

            if (buildSite != null) {
                break;
            } else {
                actions.remove(best_action);
            }
        }

        if (best_action != null) {
            //should now have something to do
            //action.execute()
            best_action.processAction();
        }
        //made an action, clear out choices for next time

        actions.clear();
    }
}
