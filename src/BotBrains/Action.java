package BotBrains;

import com.springrts.ai.oo.AIFloat3;
import com.springrts.ai.oo.clb.Unit;
import com.springrts.ai.oo.clb.UnitDef;

//TODO refactor all of this into separate classes with inheritance for the different types

public abstract class Action {

    public UnitDef def_buildeeUnit;
    public Unit def_builderUnit;
    public AIFloat3 loc_action;
    public boolean consider_me = true;

    public abstract void processAction();

    public abstract AIFloat3 findLocationForAction();

}
