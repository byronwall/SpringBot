package BotBrains;

import BotBrains.Actions.AttackAction;
import BotBrains.Actions.BuildAction;
import BotBrains.Actions.ExploreAction;
import BotBrains.Actions.NothingAction;
import BotBrains.Goals.*;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.springrts.ai.oo.AIFloat3;
import com.springrts.ai.oo.clb.OOAICallback;
import com.springrts.ai.oo.clb.Unit;
import com.springrts.ai.oo.clb.UnitDef;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by byronandanne on 11/26/2014.
 */
public class DecisionMaker {
    private static DecisionMaker maker = null;
    public DataMap ThreatMap;
    public DataMap VisitedMap;
    List<Goal> goals = new ArrayList<Goal>();
    OOAICallback clb = null;
    UnitDef unit_first = null;

    private DecisionMaker() {
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
        SpringBot.write("ready to choose");
        for (Action action : actions) {
            //skip is not for consideration
            if (!action.consider_me) {
                continue;
            }

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
                    if (a.consider_me) {
                        values.put(a, g, values.get(a, g) / max);
                    }
                }
            }
        }

        //find the best action for that goal
        Action top_action = null;
        float max_utility = -Float.MAX_VALUE;
        for (Action a : actions) {
            if (!a.consider_me) {
                continue;
            }
            //update for each action
            float utility = 0;
            for (Goal g : goals) {
                utility += values.get(a, g) * g.getGoalValue();
            }

            //SpringBot.write("action: " + a + ", util: " + utility);

            if (utility > max_utility) {
                top_action = a;
                max_utility = utility;

                //SpringBot.write("new best action: " + a.toString() + ", " + utility);
            }
        }

        //now we have modified action scores... need to sum product with goal scores to pick


        if (top_action == null) {
            SpringBot.write("No actions are available.");
        }

        return top_action;
    }

    public void ProcessUnit(Unit unit) {

        //init the first unit
        if (unit_first == null) {
            unit_first = unit.getDef();
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

        ExploreAction exploreAction = ExploreAction.createAction(unit);
        if (exploreAction != null) {
            actions.add(exploreAction);
        }


        //create the do nothing action
        if (actions.size() > 0) {
            actions.add(new NothingAction());
        }

        SpringBot.write("process unit actions: " + unit.getDef().getName() + ", count: " + actions.size());

        //size ==1 => only the do nothing option
        if (actions.size() > 0) {
            DecideAndExecute(actions);
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
        goals.add(new MakeMilitary());
        //goals.add(new ForceSingleUnit());
        goals.add(new DoNotBuildMore());
        goals.add(new BuildFactories());
        goals.add(new PursueMoreTechGoal());
    }

    public void DecideAndExecute(List<Action> actions) {
        //need to choose the best action

        //break if no action
        if (actions.size() == 0) return;

        Action best_action = null;
        AIFloat3 buildSite = null;

        int counter = 0;
        while (true) {
            SpringBot.write("counter: " + counter++);

            best_action = chooseAction(actions);

            if (best_action == null) {
                break;
            }

            SpringBot.write("best action: " + best_action.toString());

            //need to pick build location and check if it can be done

            buildSite = best_action.findLocationForAction();


            //TODO verify that this code will work as expected
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
