package BotBrains.Actions;

import BotBrains.Action;
import BotBrains.DecisionMaker;
import BotBrains.SpringBot;
import BotBrains.Util;
import com.springrts.ai.oo.AIFloat3;
import com.springrts.ai.oo.clb.OOAICallback;
import com.springrts.ai.oo.clb.Resource;
import com.springrts.ai.oo.clb.Unit;
import com.springrts.ai.oo.clb.UnitDef;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by byronandanne on 11/30/2014.
 */
public class BuildAction extends Action {

    AIFloat3 buildSite = null;

    public static List<Action> createAllActions(Unit unit) {

        if (unit.getDef().getBuildOptions().size() == 0 || unit.isBeingBuilt()) {
            return null;
        }

        List<Action> actions = new ArrayList<Action>();

        for (UnitDef build_options : unit.getDef().getBuildOptions()) {
            Action action_new = new BuildAction();
            action_new.def_buildeeUnit = build_options;
            action_new.def_builderUnit = unit;

            actions.add(action_new);
        }

        return actions;
    }

    @Override
    public void processAction() {
        if (buildSite != null) {
            this.def_builderUnit.build(this.def_buildeeUnit, buildSite, 0, (short) 0, 0);
        }
    }

    @Override
    public AIFloat3 findLocationForAction() {
        OOAICallback clb = DecisionMaker.get().getClb();

        boolean resource = false;

        for (Resource res : clb.getResources()) {
            if (this.def_buildeeUnit.getExtractsResource(res) > 0) {
                //this appears to only work for extractable resources
                float min_dist = Float.MAX_VALUE;
                resource = true;
                for (AIFloat3 resource_spot : clb.getMap().getResourceMapSpotsPositions(res)) {

                    //check distance and then verify that we can build there
                    float dist = Util.calcDist(this.def_builderUnit.getPos(), resource_spot);
                    if (dist < min_dist) {
                        //check if can build
                        if (clb.getMap().isPossibleToBuildAt(this.def_buildeeUnit, resource_spot, 0)) {
                            min_dist = dist;
                            buildSite = resource_spot;

                            //SpringBot.write("resource build: " + buildSite);
                        }
                    }
                }
            }
            //else here would handle any other location specific concenrs
        }

        if (buildSite != null) {
            if (Math.sqrt(Util.calcDist(buildSite, def_builderUnit.getPos())) > 3000) {
                buildSite = null;
            }
        }

        //will be null if there is no resource specific stuff going on
        if (!resource) {
            if (buildSite == null || buildSite == Util.EMPTY_POS) {
                //at this point, we still do not have a buildsite.  just need to pick somethign nearby

                //do somethign different for a mobile unit
                AIFloat3 possibleSite;
                if (this.def_buildeeUnit.getSpeed() > 0) {
                    //have a mobile unit, just build it somewhere close
                    possibleSite = Util.getRandomNearbyPosition(this.def_builderUnit.getPos(), 100);
                } else {
                    possibleSite = Util.getRandomNearbyPosition(this.def_builderUnit.getPos(), 100);

                    //try to avoid the expensive call
                    //TODO, determine if this helps any
                    if (!clb.getMap().isPossibleToBuildAt(this.def_builderUnit.getDef(), possibleSite, 0)) {
                        possibleSite = clb.getMap().findClosestBuildSite(this.def_buildeeUnit, this.def_builderUnit.getPos(), 5000, 5, 0);
                    }
                }

                SpringBot.write("possibleSite: " + possibleSite);

                if (!Util.PosIsNull(possibleSite)) {
                    buildSite = possibleSite;
                }

            }
        }

        //TODO do not allow the commander to go venturing too far to build something
        //this is not working as needed
        if (buildSite != null) {
            if (def_builderUnit.getDef().getName().equals("armcom") || def_builderUnit.getDef().getName().equals("corcom")) {
                AIFloat3 highestValue = DecisionMaker.get().VisitedMap.getHighestValue();

                if (Math.sqrt(Util.calcDist(buildSite, highestValue)) > 2500) {
                    buildSite = null;
                    SpringBot.write("Commander not allow to build there: " + buildSite + ", high" + highestValue);
                }
            }
        }

        //just return buildSite which may be null
        return buildSite;
    }

    @Override
    public String toString() {

        return String.format("BUILD action, unit: %s, to_build: %s", def_builderUnit.getDef().getName(), def_buildeeUnit.getName());


    }
}
