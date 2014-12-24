package BotBrains;

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
    }

    public static GroupManager get() {
        if (_instance == null) {
            _instance = new GroupManager();
        }

        return _instance;
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
}
