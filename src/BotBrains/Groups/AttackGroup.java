package BotBrains.Groups;

import BotBrains.*;
import com.springrts.ai.oo.AIFloat3;
import com.springrts.ai.oo.clb.Unit;

public class AttackGroup extends Group {

    private final float prob_join = 0;

    //TODO need to get an action for the leader
    //TODO need to get other units to guard the leader
    boolean attacking = false;
    AIFloat3 attack_pos = null;
    int lastframe = 0;
    private int ATTACK_SIZE = 6;

    public AttackGroup() {
        MAX_SIZE = 10;
    }

    @Override
    public float unitEvalMembership(Unit unit) {
        float value = 0;
        if (Util.getTotalDamage(unit.getDef()) > 0 &&
                unit.getDef().getSpeed() > 0 &&
                Util.RAND.nextFloat() > prob_join &&
                !unit.getDef().getName().equals("armcom") &&
                !unit.getDef().getName().equals("corcom")
                ) {
            value = 0.9f;
        }

        return value;
    }

    @Override
    public void addUnitToGroup(Unit unit) {
        //get the center first
        AIFloat3 center = getCenter();
        super.addUnitToGroup(unit);
        if (center != null) {
            unit.moveTo(center, (short) 0, SpringBot.FRAME + 5000);
        } else {
            //TODO determine some default location to go to
        }

        //once it's added... need to assign to leader
        DatabaseMaster.get().addFrameData(GroupManager.TABLE, "adding unit to ATTACK group, moving to center");

        //if joining, move to center of group

    }

    @Override
    public boolean processUnit(Unit unit) {
        //attack group does not need to do anything, except tell others not to touch it
        return true;
    }

    private AIFloat3 getCenter() {
        AIFloat3 pos = null;
        for (Unit unit : members.values()) {
            if (unit == null) {
                continue;
            }

            if (pos == null) {
                pos = new AIFloat3();
            }

            AIFloat3 unitPos = unit.getPos();
            pos.x += unitPos.x;
            pos.z += unitPos.z;
        }

        //have them all... take average
        if (pos != null) {
            pos.x = pos.x / members.size();
            pos.z = pos.z / members.size();
        }

        pos.x = Util.clamp(pos.x, 0, DecisionMaker.get().ThreatMap.map_width);
        pos.z = Util.clamp(pos.z, 0, DecisionMaker.get().ThreatMap.map_height);

        return pos;
    }

    @Override
    public void doTimelyTask(int frame) {
        //this is where we will decide if to attack and where

        //TODO clean up other processing including dead units and completed tasks.. also think about retreating

        AIFloat3 center = getCenter();

        DatabaseMaster.get().addFrameData(GroupManager.TABLE, "attack size is: " + members.size());
        if (members.size() < ATTACK_SIZE) {
            return;
        }

        //this is just a reset for time.
        if (frame - lastframe > 5000) {
            attacking = false;
        }

        //if attacking and not near attack point... let it go
        if (attacking) {

            if (Util.calcDist(center, attack_pos) > 500) {
                DatabaseMaster.get().addFrameData(GroupManager.TABLE, "group is attacking and not there yet");
                return;
            } else {
                attacking = false;
            }
        }

        //check that all units are close to center
        boolean dist_good = true;
        for (Unit unit : members.values()) {
            if (unit == null) {
                continue;
            }
            AIFloat3 unitPos = unit.getPos();

            if (Util.calcDist(unitPos, center) > 1000) {
                //not ready
                DatabaseMaster.get().addFrameData(GroupManager.TABLE, "ATTACK group is not close enough attack");
                if (center != null) {
                    unit.moveTo(center, (short) 0, SpringBot.FRAME + 2000);
                }

                dist_good = false;
            }
        }

        if (!dist_good) {
            return;
        }

        //pick a target
        DatabaseMaster.get().addFrameData(GroupManager.TABLE, "ATTACK group is going to attack");
        AIFloat3 highestValue = null;
        if (Util.RAND.nextFloat() > 0.5f) {
            //highestValue = DecisionMaker.get().ThreatMap.getHighestNearby(center);
            highestValue = DecisionMaker.get().ThreatMap.getHighestValue();
        } else {


            AIFloat3 randomNearbyPosition = Util.getRandomNearbyPosition(center, 4000);
            randomNearbyPosition.x = Util.clamp(randomNearbyPosition.x, 0, DecisionMaker.get().ThreatMap.map_width);
            randomNearbyPosition.z = Util.clamp(randomNearbyPosition.z, 0, DecisionMaker.get().ThreatMap.map_height);

            highestValue = randomNearbyPosition;
        }


        attack_pos = highestValue;
        attacking = true;
        lastframe = frame;

        for (Unit unit : members.values()) {
            if (unit == null) {
                continue;
            }
            //unit.setMoveState(Util.MOVE_STATE_ROAM, (short) 0, SpringBot.FRAME + 2000);
            unit.fight(highestValue, (short) 0, SpringBot.FRAME + 2000);
        }

        //attack
    }
}
