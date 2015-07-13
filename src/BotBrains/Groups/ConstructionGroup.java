package BotBrains.Groups;

import BotBrains.*;
import com.springrts.ai.oo.clb.Unit;

public class ConstructionGroup extends Group {

    private final float prob_join = 0.25f;
    Unit leader;

    //TODO need to get an action for the leader
    //TODO need to get other units to guard the leader

    @Override
    public float unitEvalMembership(Unit unit) {
        float value = 0;
        if (unit.getDef().isBuilder() && unit.getDef().getBuildOptions().size() > 0 && unit.getDef().getSpeed() > 0 && Util.RAND.nextFloat() > prob_join) {
            value = 1;
        }

        return value;
    }

    @Override
    public void addUnitToGroup(Unit unit) {
        super.addUnitToGroup(unit);

        //once it's added... need to assign to leader

        DatabaseMaster.get().addFrameData(GroupManager.TABLE, "adding unit to group");

        if (leader != null) {
            unit.guard(leader, (short) 0, SpringBot.FRAME + 1000);
        } else {
            leader = unit;
            DatabaseMaster.get().addFrameData(GroupManager.TABLE, "unit set to leader");
        }
    }

    @Override
    public boolean processUnit(Unit unit) {
        //only want to get build tasks if it is the group leader
        if (leader != null && leader.getUnitId() == unit.getUnitId()) {
            DecisionMaker.get().ProcessUnit(unit);

            DatabaseMaster.get().addFrameData(GroupManager.TABLE, "told leader to be processed");
        }

        return true;
    }

    @Override
    public void doTimelyTask(int frame) {

    }
}
