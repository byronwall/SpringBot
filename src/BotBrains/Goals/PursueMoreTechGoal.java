package BotBrains.Goals;

import BotBrains.*;
import BotBrains.Actions.BuildAction;
import BotBrains.Graph.Graph;
import BotBrains.Graph.Node;
import com.springrts.ai.oo.clb.OOAICallback;
import com.springrts.ai.oo.clb.Unit;
import com.springrts.ai.oo.clb.UnitDef;

import java.util.HashMap;

public class PursueMoreTechGoal extends Goal {

    int target_tech = 2;

    Graph tech_graph = new Graph();
    HashMap<String, Integer> distances = null;

    //constructor needs to build the tech graph
    public PursueMoreTechGoal() {

        //SpringBot.write("building the tech graph");
        //build the tree for everybody
        for (UnitDef unitDef : DecisionMaker.get().getClb().getUnitDefs()) {
            //SpringBot.write("adding in unit: " + unitDef.getName());
            if (!tech_graph.nodeExists(unitDef.getName())) {
                tech_graph.addNode(unitDef.getName());
            }

            //add in connections for all build options
            for (UnitDef def_build : unitDef.getBuildOptions()) {
                //SpringBot.write("adding in connection: " + def_build.getName());
                tech_graph.addConnection(unitDef.getName(), def_build.getName());


            }
        }
        //SpringBot.write("done creating graph");

    }

    @Override
    protected float recalculateValue() {
        //this needs to determine how to increase tech tree
        //SpringBot.write("ready to calculate tech value...");
        //need to init first
        if (distances == null) {
            //SpringBot.write("need to init distance...");
            initDistances();
        }

        //need to calculate average distance for all units
        OOAICallback clb = DecisionMaker.get().getClb();
        int total_distance = 0;
        int total_units = 0;
        for (Unit unit : clb.getTeamUnits()) {
            //SpringBot.write("getting distance for :" + unit.getDef().getName());
            total_distance += distances.get(unit.getDef().getName());
            total_units++;
        }

        float avg_dist = 1.0f * total_distance / total_units;
        float value = Util.clamp((target_tech - avg_dist) / target_tech, 0, 1);

        SpringBot.write("tech goal value is: " + value + " w/ " + avg_dist);

        return value;
    }

    private void initDistances() {
        //SpringBot.write("calculating tech distances...");
        distances = new HashMap<String, Integer>();

        UnitDef unit_first = DecisionMaker.get().get_firstUnit();
        //SpringBot.write("first unit: " + unit_first.getName());
        Node root = tech_graph.nodes.get(unit_first.getName());

        distances.put(unit_first.getName(), 0);

        traverseNext(root, 1);

        //write out results to check
        //SpringBot.write("distances from: " + unit_first.getName());
        for (String s : distances.keySet()) {
            SpringBot.write(s + " = " + distances.get(s));
        }
        //need to go from node, and record every other node that is hit

    }

    private void traverseNext(Node root, int depth) {
        //SpringBot.write("node: " + root.data + ", depth: " + depth);

        for (Node node_next : root.getOutbound()) {
            //SpringBot.write("node: " + node_next.data + ", exists?: " + distances.containsKey(node_next.data));
            //need to keep searching if:
            //new node
            //better distance to existing node/
            //return when hitting an old one with worse or same value
            if (!distances.containsKey(node_next.data)) {
                distances.put(node_next.data, depth);
                traverseNext(node_next, depth + 1);
            } else if (depth < distances.get(node_next.data)) {
                distances.put(node_next.data, depth);
                traverseNext(node_next, depth + 1);
            }
        }
    }

    //TODO: need to cache some of these results.  they will not change later.

    @Override
    public float getGoalChange(Action action) {
        if (distances == null) {
            initDistances();
        }

        if (action instanceof BuildAction) {
            //going to build... how good is this unit

            //this will be a 0 or 1 choice... is it a factory?

            UnitDef def = action.def_buildeeUnit;

            //SpringBot.write("calculating distance for: " + def.getName());

            int distance =  distances.get(def.getName());

            //SpringBot.write("distance is: " + distance);
            int child_dist = 1;
            //give credit for build queue, likely need to tweak this
            for (UnitDef unitDef : def.getBuildOptions()) {
                Integer child_unit = distances.get(unitDef.getName());
                //SpringBot.write("checking children now: " + unitDef.getName() + ", val: " + child_unit);
                child_dist += child_unit;
            }

            distance += Math.log10(child_dist);

            //SpringBot.write("final distance value: " + distance);
            return distance;

        }

        return 0;
    }
}

