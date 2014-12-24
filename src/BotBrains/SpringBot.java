package BotBrains;


import BotBrains.Tasks.GenericOneTimeTask;
import BotBrains.Tasks.GenericRecurringTask;
import com.springrts.ai.oo.AIFloat3;
import com.springrts.ai.oo.clb.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.Properties;

public class SpringBot extends com.springrts.ai.oo.AbstractOOAI {

    public static int FRAME = 0;
    long millis;
    private int teamId = -1;
    private Properties info = null;
    private Properties optionValues = null;
    private OOAICallback clb = null;

    public SpringBot() {
    }

    public static void write(String message) {
        //add a note to the DB also
        DatabaseMaster.get().addFrameData("log", message, FRAME);

    }

    public static void logError(Exception e) {
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        e.printStackTrace(printWriter);
        printWriter.flush();

        DatabaseMaster.get().addFrameData("errors", writer.toString(), FRAME);
    }

    @Override
    public int init(int skirmishAIId, OOAICallback callback) {

        //set this up first and center
        millis = System.currentTimeMillis();

        this.clb = callback;
        this.teamId = clb.getSkirmishAI().getTeamId();

        try {
            DatabaseMaster.get().setup("database " + teamId + " " + System.currentTimeMillis());
        } catch (SQLException e) {
            //avoiding the DB here since this is DB related error
            e.printStackTrace();
        }

        info = new Properties();
        Info inf = clb.getSkirmishAI().getInfo();
        int numInfo = inf.getSize();
        for (int i = 0; i < numInfo; i++) {
            String key = inf.getKey(i);
            String value = inf.getValue(i);
            info.setProperty(key, value);
        }

        optionValues = new Properties();
        OptionValues opVals = clb.getSkirmishAI().getOptionValues();
        int numOpVals = opVals.getSize();
        for (int i = 0; i < numOpVals; i++) {
            String key = opVals.getKey(i);
            String value = opVals.getValue(i);
            optionValues.setProperty(key, value);
        }


        //TODO consider deleting this stuff or deciding what is really needed
        DatabaseMaster.get().quickLog("initializing team " + teamId);
        DatabaseMaster.get().quickLog("info:" + info.toString());
        DatabaseMaster.get().quickLog("options:" + optionValues.toString());

        //write out some map info
        DatabaseMaster.get().quickLog("map name: " + clb.getMap().getName());
        DatabaseMaster.get().quickLog("map map_width: " + clb.getMap().getWidth());
        DatabaseMaster.get().quickLog("map height: " + clb.getMap().getHeight());

        //dump out all the units
        //TODO move this out to another method and call when actually needed?
        for (UnitDef def : clb.getUnitDefs()) {

            String table = "units";
            String unitName = def.getName();

            DatabaseMaster.get().addRowColTable(table, unitName, "name", unitName);
            DatabaseMaster.get().addRowColTable(table, unitName, "human name", def.getHumanName());
            DatabaseMaster.get().addRowColTable(table, unitName, "isBuilder", def.isBuilder() + "");
            DatabaseMaster.get().addRowColTable(table, unitName, "isAbleToReapir", def.isAbleToRepair() + "");
            DatabaseMaster.get().addRowColTable(table, unitName, "speed", def.getSpeed() + "");
            DatabaseMaster.get().addRowColTable(table, unitName, "weaponRange", def.getMaxWeaponRange() + "");

            for (Resource resource : clb.getResources()) {
                String res = resource.getName() + "-";

                DatabaseMaster.get().addRowColTable(table, unitName, res + "getExtracts", def.getExtractsResource(resource) + "");
                DatabaseMaster.get().addRowColTable(table, unitName, res + "getMakes", def.getMakesResource(resource) + "");
                DatabaseMaster.get().addRowColTable(table, unitName, res + "getResourceMake", def.getResourceMake(resource) + "");
                DatabaseMaster.get().addRowColTable(table, unitName, res + "getStorage", def.getStorage(resource) + "");
                DatabaseMaster.get().addRowColTable(table, unitName, res + "getUpkeep", def.getUpkeep(resource) + "");
                DatabaseMaster.get().addRowColTable(table, unitName, res + "getCost", def.getCost(resource) + "");
            }

            String units = "";
            for (UnitDef unitDef : def.getBuildOptions()) {
                units += unitDef.getName() + " ";
            }

            DatabaseMaster.get().addRowColTable(table, unitName, "buildOptions", units);
        }

        //TODO move this setup code to a spot that is not this init... too much code here
        try {
            DecisionMaker.get().setCallback(callback);
            DecisionMaker.get().InitializeGoals();

            //TODO move this to a better spot
            DecisionMaker.get().ThreatMap = new DataMap("threat levels", clb.getMap().getWidth() * 8, clb.getMap().getHeight() * 8);
            DecisionMaker.get().VisitedMap = new DataMap("unit visit map", clb.getMap().getWidth() * 8, clb.getMap().getHeight() * 8);

            //create some recurring tasks for the maps
            TaskManager.get().addTask(new GenericRecurringTask(
                    2000, 0,
                    (frame) -> {
                        DecisionMaker.get().ThreatMap.toImage("threat" + clb.getSkirmishAI().getTeamId());
                        DecisionMaker.get().ThreatMap.decay();
                        DecisionMaker.get().ThreatMap.blur();
                    }
            ));

            TaskManager.get().addTask(new GenericRecurringTask(
                    2000, 1000,
                    (frame) -> {
                        //TODO move this to a proper spot
                        DecisionMaker.get().VisitedMap.toImage("visit" + clb.getSkirmishAI().getTeamId());
                        DecisionMaker.get().VisitedMap.decay();
                        DecisionMaker.get().VisitedMap.blur();

                    }
            ));

            //update visited map
            TaskManager.get().addTask(new GenericRecurringTask(
                    500, 10 * teamId,
                    (frame) -> {
                        int count = 0;
                        for (Unit unit : clb.getTeamUnits()) {

                            if (!unit.isBeingBuilt()) {
                                if (unit.getCurrentCommands().size() == 0 || unit.getCurrentCommands().get(0).getId() == 0) {
                                    //SpringBot.write("idle unit in action now...: " + unit.getDef().getName() + unit.getUnitId());


                                    TaskManager.get().addTask(new GenericOneTimeTask(frame + count++, (c) -> DecisionMaker.get().ProcessUnit(unit)));
                                    //DecisionMaker.get().ProcessUnit(unit);
                                }
                            }
                        }

                        //reset goals for next update... should go faster this way
                        //TODO move this to a proper spot
                        DecisionMaker.get().resetGoals();

                        trackTime(frame);

                    }
            ));

            TaskManager.get().addTask(new GenericRecurringTask(
                    100, 2,
                    (frame) -> {
                        for (Unit unit : clb.getTeamUnits()) {
                            if (unit.getDef().getSpeed() > 0) {
                                DecisionMaker.get().VisitedMap.addToMap(unit.getPos(), 3);
                            }
                        }
                    }
            ));

            TaskManager.get().addTask(new GenericRecurringTask(
                    1000, 23,
                    (frame) -> {
                        DatabaseMaster.get().commitData();
                    }
            ));

            //set up the database


        } catch (Exception e) {
            SpringBot.logError(e);
        }

        return 0;
    }

    @Override
    public int release(int reason) {

        //ensures that the pending entries are cleared
        DatabaseMaster.get().commitData();

        return 0; // signaling: OK
    }

    public void trackTime(int frame) {
        long elapsed = System.currentTimeMillis() - millis;

        DatabaseMaster.get().addFrameData("FRAMES", String.valueOf(elapsed), FRAME);
    }

    @Override
    public int update(int frame) {
        FRAME = frame;
        try {
            //process all units every so often
            TaskManager.get().processTasks(frame);

        } catch (Exception e) {
            SpringBot.logError(e);
        }

        return 0; // signaling: OK

    }


    @Override
    public int unitCreated(Unit unit, Unit builder) {

        return 0;
    }

    @Override
    public int unitFinished(Unit unit) {

        try {
            //process all units every so often
            unit.setOn(true, (short) 0, 0);

            //TODO optmize this group deferal code.. not good to only have it here
            //this will evaluate group addition for the current unit
            //if in a group.. defer to that group's process scehem
            Group group = GroupManager.get().evaluateUnitForGroups(unit);
            if (group != null) {
                group.processUnit(unit);
            } else {
                DecisionMaker.get().ProcessUnit(unit);
            }

        } catch (Exception e) {
            SpringBot.logError(e);
        }


        return 0;
    }

    @Override
    public int unitIdle(Unit unit) {

        try {
            //process all units every so often
            TaskManager.get().addTask(new GenericOneTimeTask(1, (c) -> DecisionMaker.get().ProcessUnit(unit)));

        } catch (Exception e) {
            SpringBot.logError(e);
        }

        return 0; // signaling: OK
    }


    @Override
    public int unitDamaged(Unit unit, Unit attacker, float damage, AIFloat3 dir, WeaponDef weaponDef, boolean paralyzed) {
        try {
            //TODO handle these events better
            //SpringBot.write("unit damaged: " + unit.getDef().getName() + "," + unit.getPos());
            DecisionMaker.get().ThreatMap.addToMap(unit.getPos(), 2);
        } catch (Exception e) {
            SpringBot.logError(e);
        }

        return 0; // signaling: OK
    }

    @Override
    public int unitDestroyed(Unit unit, Unit attacker) {
        try {
            //TODO handle these events better
            //SpringBot.write("unit destroyed: " + unit.getDef().getName() + "," + unit.getPos());
            DecisionMaker.get().ThreatMap.addToMap(unit.getPos(), 5);

            Intelligence.unitKill(attacker, unit);

        } catch (Exception e) {
            SpringBot.logError(e);
        }

        return 0; // signaling: OK
    }


    @Override
    public int enemyEnterLOS(Unit enemy) {
        try {
            //TODO handle these events better
            if (enemy != null) {
                //SpringBot.write("enemy spotted: " + enemy.getDef().getName() + "," + enemy.getPos());
                DecisionMaker.get().ThreatMap.addToMap(enemy.getPos(), 2);
            }
        } catch (Exception e) {
            SpringBot.logError(e);
        }

        return 0;
    }


    @Override
    public int enemyEnterRadar(Unit enemy) {
        try {
            //TODO handle these events better
            if (enemy != null) {
                //SpringBot.write("enemy spotted: " + enemy.getDef().getName() + "," + enemy.getPos());
                DecisionMaker.get().ThreatMap.addToMap(enemy.getPos(), 1);
            }
        } catch (Exception e) {
            SpringBot.logError(e);
        }

        return 0; // signaling: OK
    }


    @Override
    public int enemyDestroyed(Unit enemy, Unit attacker) {
        Intelligence.unitKill(attacker, enemy);

        return 0; // signaling: OK
    }

    /*
    THIS REMOVES OVERRIDES THAT ARE NOT BEING USED RIGHT NOW

    @Override
    public int message(int player, String message) {
        return 0; // signaling: OK
    }

    @Override
    public int unitMoveFailed(Unit unit) {
        return 0; // signaling: OK
    }

    @Override
    public int unitGiven(Unit unit, int oldTeamId, int newTeamId) {
        return 0; // signaling: OK
    }

    @Override
    public int unitCaptured(Unit unit, int oldTeamId, int newTeamId) {
        return 0; // signaling: OK
    }

    @Override
    public int enemyLeaveLOS(Unit enemy) {
        return 0; // signaling: OK
    }

    @Override
    public int enemyLeaveRadar(Unit enemy) {
        return 0; // signaling: OK
    }

    @Override
    public int enemyDamaged(Unit enemy, Unit attacker, float damage, AIFloat3 dir, WeaponDef weaponDef, boolean paralyzed) {
        return 0; // signaling: OK
    }

    @Override
    public int weaponFired(Unit unit, WeaponDef weaponDef) {
        return 0; // signaling: OK
    }

    @Override
    public int playerCommand(java.util.List<Unit> units, int commandTopicId, int playerId) {
        return 0; // signaling: OK
    }

    @Override
    public int commandFinished(Unit unit, int commandId, int commandTopicId) {
        return 0; // signaling: OK
    }

    @Override
    public int seismicPing(AIFloat3 pos, float strength) {
        return 0; // signaling: OK
    }

    @Override
    public int load(String file) {
        return 0; // signaling: OK
    }

    @Override
    public int save(String file) {
        return 0; // signaling: OK
    }

    @Override
    public int enemyCreated(Unit enemy) {
        return 0; // signaling: OK
    }

    @Override
    public int enemyFinished(Unit enemy) {
        return 0; // signaling: OK
    }

     */

}