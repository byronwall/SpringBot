package BotBrains.Goals;

import BotBrains.Action;
import BotBrains.DecisionMaker;
import BotBrains.Goal;
import BotBrains.SpringBot;
import com.springrts.ai.oo.clb.Unit;
import com.springrts.ai.oo.clb.UnitDef;
import com.springrts.ai.oo.clb.WeaponMount;

public class MakeMilitary extends Goal {

    @Override
    protected float recalculateValue() {
        //this needs to determine how important this goal is

        //assume that the military needs to be some percent of the total units


        int units_total = 0;
        int units_military = 0;
        for (Unit unit : DecisionMaker.get().getClb().getTeamUnits()) {
            units_total++;
            UnitDef def = unit.getDef();
            if (!def.isBuilder() && def.getWeaponMounts().size() > 0) {
                units_military++;
            }
        }

        //don't start with military
        float value = 0;

        if (units_total > 5) {
            value = 1 - 2.0f * units_military / units_total;
        }

        SpringBot.write("military ratio goal: " + value + ", w/ " + units_total);

        return value;

    }

    @Override
    public float getGoalChange(Action action) {

        if (action.type == Action.ActionType.BUILD) {
            //going to build... how good is this unit
            //this just picks the one with the biggest guns
            //TODO: somehow factor in unit speed, range, cost, etc.

            return getDamage(action.def_buildeeUnit);

        } else {
            //no build = no care
            return 0;
        }

    }

    //TODO: cache this result for all  units... will not change
    private float getDamageSiblings(UnitDef def) {
        //idea is that the ability to build units is important.
        //add in child units with average damage times sqrt(number of units).
        //this allows mild growth in rating with more possible units

        float damage = getDamage(def);

        for (UnitDef def_child : def.getBuildOptions()) {
            damage += getDamage(def_child) / def.getBuildOptions().size() * 0.1f;
        }

        return damage;
    }

    private float getDamage(UnitDef def) {

        float damage = 0;

        for (WeaponMount weaponMount : def.getWeaponMounts()) {
            if (weaponMount.getWeaponDef().isAbleToAttackGround() && !weaponMount.getWeaponDef().isFireSubmersed()) {
                damage += weaponMount.getWeaponDef().getDamage().getTypes().get(0) * weaponMount.getWeaponDef().getAreaOfEffect() * weaponMount.getWeaponDef().getSalvoSize();
            }
        }

        return damage;
    }
}