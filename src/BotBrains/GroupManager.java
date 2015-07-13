package BotBrains;

import BotBrains.Groups.AttackGroup;
import BotBrains.Groups.ConstructionGroup;
import com.springrts.ai.oo.clb.Unit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by byronandanne on 12/23/2014.
 */
public class GroupManager {

    //TODO need to create a military group
    //TODO need to handle when units die

    public static String TABLE = "groups";
    static GroupManager _instance = null;
    List<Group> groups = new ArrayList<>();
    HashMap<Integer, Group> assignments = new HashMap<>();

    public GroupManager() {
        //load it up with some blank groups for now
        groups.add(new ConstructionGroup());
        groups.add(new AttackGroup());
    }

    public static GroupManager get() {
        if (_instance == null) {
            _instance = new GroupManager();
        }

        return _instance;
    }

    public boolean processUnit(Unit unit) {
        if (assignments.containsKey(unit.getUnitId())) {
            Group group = assignments.get(unit.getUnitId());
            if (group != null) {
                return group.processUnit(unit);
            }
        }

        return false;
    }

    //TODO this may want a way to reprocess units later
    public Group evaluateUnitForGroups(Unit unit) {
        //need to iterate groups and place in the best group

        if (!assignments.containsKey(unit.getUnitId())) {

            float max_membership = 0;
            Group best_group = null;
            for (Group group : groups) {
                if (group.members.size() >= group.MAX_SIZE) {
                    continue;
                }

                float v = group.unitEvalMembership(unit);
                if (v > max_membership) {
                    best_group = group;
                }
            }

            if (best_group != null) {
                best_group.addUnitToGroup(unit);

                if (best_group.members.size() >= best_group.MAX_SIZE) {
                    //hopefully this adds a new one
                    try {
                        groups.add(best_group.getClass().newInstance());
                    } catch (InstantiationException e) {
                        SpringBot.logError(e);
                    } catch (IllegalAccessException e) {
                        SpringBot.logError(e);
                    }
                }
            }

            //add to hash
            assignments.put(unit.getUnitId(), best_group);


            return best_group;
        } else {
            return assignments.get(unit.getUnitId());
        }

    }

    public void doTimelyTasks(int frame) {
        DatabaseMaster.get().addFrameData(TABLE, "timely task for groups");
        for (Group group : groups) {

            //need to clean out dead things first
            for (Integer integer : group.members.keySet()) {
                if(group.members.get(integer) == null){
                    group.members.remove(integer);
                }
            }


            group.doTimelyTask(frame);
        }

    }
}
