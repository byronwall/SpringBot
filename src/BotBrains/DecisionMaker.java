package BotBrains;

import BotBrains.Goals.*;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.springrts.ai.oo.AIFloat3;
import com.springrts.ai.oo.clb.OOAICallback;
import com.springrts.ai.oo.clb.Resource;
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
        CreateBuildActions(unit, actions);
        //SpringBot.write("creating attack actions for :" + unit.getDef().getName());
        CreateFightAction(unit, actions);

        //create the do nothing action
        if (actions.size() > 0) {
            actions.add(new Action(Action.ActionType.DO_NOTHING, unit));
        }

        SpringBot.write("process unit actions: " + unit.getDef().getName() + ", count: " + actions.size());

        //size ==1 => only the do nothing option
        if (actions.size() > 0) {
            DecideAndExecute(actions);
        }

    }

    public void CreateBuildActions(Unit unit, List<Action> actions) {
        //this needs to create the set of build actions

        for (UnitDef build_options : unit.getDef().getBuildOptions()) {
            Action action_new = new Action();
            action_new.type = Action.ActionType.BUILD;
            action_new.def_buildeeUnit = build_options;
            action_new.def_builderUnit = unit;

            actions.add(action_new);
        }
    }

    public void CreateExploreAction(Unit unit, List<Action> actions) {

        //check for weapons, return if none
        if (unit.getDef().getSpeed() == 0) {
            return;
        }

        Action action_explore = new Action();

        action_explore.type = Action.ActionType.EXPLORE;
        action_explore.def_builderUnit = unit;


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


        AIFloat3 pos_map = VisitedMap.getLowestValue();

        //choose between the closer location
        if(Util.calcDist(unit.getPos(), pos) < Util.calcDist(unit.getPos(), pos_map)){
            action_explore.loc_action = pos;
        }
        else{
            action_explore.loc_action = pos_map;
        }

        SpringBot.write("EXPLORE action added:" + action_explore);

        actions.add(action_explore);
    }

    public void CreateFightAction(Unit unit, List<Action> actions) {

        //check for weapons, return if none
        if (unit.getDef().getWeaponMounts().size() == 0 ||
                !unit.getDef().isAbleToMove() ||
                unit.getDef().isCommander() ||
                unit.getDef().getName().equals("armcom") ||
                unit.getDef().getName().equals("corcom")) {
            return;
        }

        Action fight = new Action();

        fight.type = Action.ActionType.ATTACK;
        fight.def_builderUnit = unit;
        fight.loc_action = ThreatMap.getHighestValue();

        SpringBot.write("attack action added:" + fight);

        actions.add(fight);
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
            if (best_action.type == Action.ActionType.BUILD) {
                for (Resource res : getClb().getResources()) {
                    if (best_action.def_buildeeUnit.getExtractsResource(res) > 0) {
                        //this appears to only work for extractable resources
                        float min_dist = Float.MAX_VALUE;
                        for (AIFloat3 resource_spot : clb.getMap().getResourceMapSpotsPositions(res)) {

                            //check distance and then verify that we can build there
                            float dist = Util.calcDist(best_action.def_builderUnit.getPos(), resource_spot);
                            if (dist < min_dist) {
                                //check if can build
                                if (clb.getMap().isPossibleToBuildAt(best_action.def_buildeeUnit, resource_spot, 0)) {
                                    min_dist = dist;
                                    buildSite = resource_spot;

                                    //SpringBot.write("resource build: " + buildSite);
                                }
                            }
                        }
                    }
                    //else here would handle any other location specific concenrs
                }
            }


            if (best_action.type == Action.ActionType.BUILD) {
                //will be null if there is no resource specific stuff going on
                if (buildSite == null || buildSite == Util.EMPTY_POS) {
                    //at this point, we still do not have a buildsite.  just need to pick somethign nearby
                    AIFloat3 possibleSite = clb.getMap().findClosestBuildSite(best_action.def_buildeeUnit, best_action.def_builderUnit.getPos(), 5000, 5, 0);

                    SpringBot.write("possibleSite: " + possibleSite);

                    if (!Util.PosIsNull(possibleSite)) {
                        buildSite = possibleSite;
                    }

                }

                if (buildSite != null) {
                    SpringBot.write("buildSite: " + buildSite.toString());
                    break;
                } else {
                    //if not doable, need to delete it and check again
                    best_action.consider_me = false;
                }
            } else if (best_action.type == Action.ActionType.ATTACK ||
                    best_action.type == Action.ActionType.DO_NOTHING ||
                    best_action.type == Action.ActionType.EXPLORE) {
                //these all do not require further deliberation... at least not yet
                break;
            }

        }

        if (best_action != null) {
            //should now have something to do
            //action.execute()

            switch (best_action.type) {
                case BUILD:
                    best_action.def_builderUnit.build(best_action.def_buildeeUnit, buildSite, 0, (short) 0, 0);
                    break;
                case EXPLORE:
                    best_action.def_builderUnit.setMoveState(Util.MOVE_STATE_ROAM, (short) 0, 0);
                    best_action.def_builderUnit.moveTo(best_action.loc_action, (short) 0, 0);
                    break;
                case ATTACK:
                    SpringBot.write("going to attack :" + best_action + ", loc: " + best_action.loc_action);

                    //set to roam.... hopefully he attacks now.
                    best_action.def_builderUnit.setMoveState(Util.MOVE_STATE_ROAM, (short) 0, 0);
                    best_action.def_builderUnit.fight(best_action.loc_action, (short) 0, 0);

                    break;
                case DO_NOTHING:
                    break;
            }


        }
        //made an action, clear out choices for next time

        actions.clear();
    }
}
