package BotBrains;

import com.springrts.ai.oo.clb.Unit;

/**
 * Created by byronandanne on 12/16/2014.
 */
public class Intelligence {

    public static final String KILL_TABLE = "kill_tracker";

    public static void unitKill(Unit killer, Unit got_killed) {
        //there will be a database table for tracking this
        //will work to determine which units to create

        if (killer == null || got_killed == null) {
            return;
        }

        DatabaseMaster.get().addRowColTable(
                KILL_TABLE,
                killer.getDef().getName(),
                got_killed.getDef().getName(),
                String.valueOf(SpringBot.FRAME));

        //later this may do more.
    }


}
