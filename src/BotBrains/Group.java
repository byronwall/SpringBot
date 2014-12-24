package BotBrains;

import com.springrts.ai.oo.clb.Unit;

import java.util.HashMap;

/**
 * Created by byronandanne on 12/23/2014.
 */
public abstract class Group {

    //TODO need to track members
    //TODO need to have an action
    //TODO need to determine how well a unit fits into a group
    //TODO need to return group specific info for others to consume
    //TODO extending classes will fill in the specific logic

    HashMap<Integer, Unit> members;

    //TODO do this better
    Integer MAX_SIZE = 5;

    public Group() {
        members = new HashMap<>();
    }

    public float unitEvalMembership(Unit unit) {
        return 0;
    }

    public void addUnitToGroup(Unit unit) {
        members.put(unit.getUnitId(), unit);
    }

    public void processUnit(Unit unit) {
        DecisionMaker.get().ProcessUnit(unit);
    }
}

