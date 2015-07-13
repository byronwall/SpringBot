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

    protected HashMap<Integer, Unit> members;

    //TODO do this better
    protected int MAX_SIZE = 6;

    public Group() {
        members = new HashMap<>();
    }

    public float unitEvalMembership(Unit unit) {
        return 0;
    }

    public void addUnitToGroup(Unit unit) {
        members.put(unit.getUnitId(), unit);
    }

    public boolean processUnit(Unit unit) { return false;    }

    public abstract void doTimelyTask(int frame);
}

