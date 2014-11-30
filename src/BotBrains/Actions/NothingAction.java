package BotBrains.Actions;

import BotBrains.Action;
import com.springrts.ai.oo.AIFloat3;

/**
 * Created by byronandanne on 11/30/2014.
 */
public class NothingAction extends Action {


    @Override
    public void processAction() {

    }

    @Override
    public AIFloat3 findLocationForAction() {

        //TODO fix this garbage
        return new AIFloat3(0, 0, 0);
    }
}
