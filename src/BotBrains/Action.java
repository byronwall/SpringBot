package BotBrains;

import com.springrts.ai.oo.AIFloat3;
import com.springrts.ai.oo.clb.Unit;
import com.springrts.ai.oo.clb.UnitDef;

//TODO refactor all of this into separate classes with inheritance for the different types

/**
 * Created by byronandanne on 11/26/2014.
 */
public class Action {

    public enum ActionType {BUILD, EXPLORE, ATTACK, DO_NOTHING}
    public ActionType type;
    public UnitDef def_buildeeUnit;
    public Unit def_builderUnit;
    public AIFloat3 loc_action;
    public boolean consider_me = true;

    public Action(){}

    public Action(ActionType type, Unit def_builderUnit) {
        this.type = type;
        this.def_builderUnit = def_builderUnit;
    }

    @Override
    public String toString() {
        switch (type) {
            case BUILD:
                return String.format("type: %s, unit: %s, to_build: %s", type, def_builderUnit.getDef().getName(), def_buildeeUnit.getName());

            case EXPLORE:
                return String.format("type: %s, unit: %s, loc: %s", type, def_builderUnit.getDef().getName(), loc_action);

            case ATTACK:
                return String.format("type: %s, unit: %s, loc: %s", type, def_builderUnit.getDef().getName(), loc_action);

            case DO_NOTHING:
                return String.format("type: %s, unit: %s", type, def_builderUnit.getDef().getName());

        }
        return "no type";
    }
}
