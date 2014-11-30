package BotBrains;


import com.springrts.ai.oo.AIFloat3;
import com.springrts.ai.oo.clb.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.logging.*;

public class SpringBot extends com.springrts.ai.oo.AbstractOOAI {

    private static final int DEFAULT_ZONE = 0;
    public static Logger log = null;
    private int skirmishAIId = -1;
    private int teamId = -1;
    private Properties info = null;
    private Properties optionValues = null;
    private OOAICallback clb = null;
    private String myLogFile = null;
    private Unit commander;
    private Random rand = new Random();
    private AIFloat3 loc_lastEnemy = null;

    public SpringBot() {
    }

    public static void write(String message) {
        log.log(Level.FINE, message);
    }

    private static void logProperties(Logger log, Level level, Properties props) {

        log.log(level, "properties (items: " + props.size() + "):");
        for (String key : props.stringPropertyNames()) {
            log.log(level, key + " = " + props.getProperty(key));
        }
    }

    private int sendTextMsg(String msg) {

        try {
            clb.getGame().sendTextMessage(msg, DEFAULT_ZONE);
        } catch (Exception ex) {
            ex.printStackTrace();
            return 1;
        }

        return 0;
    }

    public boolean isDebugging() {
        return true;
    }

    @Override
    public int init(int skirmishAIId, OOAICallback callback) {

        int ret = -1;

        this.skirmishAIId = skirmishAIId;
        this.clb = callback;

        this.teamId = clb.getSkirmishAI().getTeamId();

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

        // initialize the log
        try {

            myLogFile = callback.getDataDirs().allocatePath((new Date()).toInstant().getEpochSecond() + " log-team-" + teamId + ".txt", true, true, false, false);
            FileHandler fileLogger = new FileHandler(myLogFile, false);
            fileLogger.setFormatter(new MyCustomLogFormatter());
            fileLogger.setLevel(Level.ALL);
            log = Logger.getLogger("springbot" + teamId);
            log.addHandler(fileLogger);

            if (isDebugging()) {
                log.setLevel(Level.ALL);
            } else {
                log.setLevel(Level.INFO);
            }
        } catch (Exception ex) {
            System.out.println("SpringBot: Failed initializing the logger!");
            sendTextMsg("Failed initializng");
            ex.printStackTrace();
            ret = -2;
        }

        try {
            log.info("initializing team " + teamId);

            log.log(Level.FINE, "info:");
            logProperties(log, Level.FINE, info);

            log.log(Level.FINE, "options:");
            logProperties(log, Level.FINE, optionValues);

            ret = 0;
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Failed initializing", ex);
            ret = -3;
        }

        //write out some map info
        log.log(Level.SEVERE, "map name: " + clb.getMap().getName());
        log.log(Level.SEVERE, "map map_width: " + clb.getMap().getWidth());
        log.log(Level.SEVERE, "map height: " + clb.getMap().getHeight());

        //dump out all the units
        List<UnitDef> unitDefs = clb.getUnitDefs();
        for (UnitDef def : unitDefs) {
            StringBuilder sb = new StringBuilder();

            sb.append(def.getName());
            sb.append("\t");
            sb.append(def.getHumanName());
            sb.append("\t");
            sb.append(def.isBuilder());
            sb.append("\t");
            sb.append(def.getSpeed());

            for (Resource resource : clb.getResources()) {
                sb.append("\t");
                sb.append(resource.getName());
                sb.append("\t");
                sb.append(def.getExtractsResource(resource));
                sb.append("\t");
                sb.append(def.getMakesResource(resource));
                sb.append("\t");
                sb.append(def.getResourceMake(resource));
                sb.append("\t");
                sb.append(def.getStorage(resource));
                sb.append("\t");
                sb.append(def.getUpkeep(resource));
                sb.append("\t");
                sb.append(def.getCost(resource));
            }

            for (UnitDef unitDef : def.getBuildOptions()) {
                sb.append("\t");
                sb.append(unitDef.getName());
            }


            log.log(Level.SEVERE, sb.toString());
        }

        ///TIME TO CREATE DecisionMaker
        DecisionMaker.get().setCallback(callback);
        DecisionMaker.get().InitializeGoals();

        //TODO move this to a better spot
        DecisionMaker.get().ThreatMap = new DataMap("threat levels", 12, clb.getMap().getWidth() * 8, clb.getMap().getHeight() * 8);
        DecisionMaker.get().VisitedMap = new DataMap("unit visit map", 12, clb.getMap().getWidth() * 8, clb.getMap().getHeight() * 8);

        return ret;
    }

    @Override
    public int release(int reason) {
        return 0; // signaling: OK
    }

    @Override
    public int update(int frame) {
        try {
            //process all units every so often
            if (frame % 400 == 0) {
                SpringBot.write("frame: " + frame);
                for (Unit unit : clb.getTeamUnits()) {

                    if (unit.getCurrentCommands().size() == 0 || unit.getCurrentCommands().get(0).getId() == 0) {
                        SpringBot.write("idle unit in action now...: " + unit.getDef().getName() + unit.getUnitId());
                        DecisionMaker.get().ProcessUnit(unit);
                    }
                }

                //reset goals for next update... should go faster this way
                //TODO move this to a proper spot
                DecisionMaker.get().resetGoals();
            }
            if (frame % 2000 == 0) {
                //TODO move this to a proper spot
                DecisionMaker.get().ThreatMap.decay();
                SpringBot.write("threat map: " + DecisionMaker.get().ThreatMap.dataToString());

                //iterate units and update visit map
                for (Unit unit : clb.getTeamUnits()) {
                    DecisionMaker.get().VisitedMap.addToMap(unit.getPos(), 1);
                }
                SpringBot.write("visit map: " + DecisionMaker.get().VisitedMap.dataToString());

            }
        } catch (Throwable t) {
            log.log(Level.SEVERE, "update " + t);
        }

        return 0; // signaling: OK

    }

    @Override
    public int message(int player, String message) {
        return 0; // signaling: OK
    }

    @Override
    public int unitCreated(Unit unit, Unit builder) {

        signalTextAndLog("Unit created: " + unit.getDef().getName());

        return 0;
    }

    @Override
    public int unitFinished(Unit unit) {

        return 0;
    }

    public void signalTextAndLog(String message) {
        signalTextAndLog(message, Level.FINE);
    }

    public void signalTextAndLog(String message, Level level) {

        message = "TEAM " + clb.getSkirmishAI().getTeamId() + "\t" + message;

        if (level == Level.SEVERE) {
            sendTextMsg(message);
        }
        log.log(level, message);
    }

    @Override
    public int unitIdle(Unit unit) {

        return 0; // signaling: OK
    }

    @Override
    public int unitMoveFailed(Unit unit) {
        return 0; // signaling: OK
    }

    @Override
    public int unitDamaged(Unit unit, Unit attacker, float damage, AIFloat3 dir, WeaponDef weaponDef, boolean paralyzed) {
        try {
            //TODO handle these events better
            SpringBot.write("unit damaged: " + unit.getDef().getName() + "," + unit.getPos());
            DecisionMaker.get().ThreatMap.addToMap(unit.getPos(), 2);
        } catch (Throwable t) {
            log.log(Level.SEVERE, "unitDamaged " + t);
        }

        return 0; // signaling: OK
    }

    @Override
    public int unitDestroyed(Unit unit, Unit attacker) {
        try {
            //TODO handle these events better
            SpringBot.write("unit destroyed: " + unit.getDef().getName() + "," + unit.getPos());
            DecisionMaker.get().ThreatMap.addToMap(unit.getPos(), 5);
        } catch (Throwable t) {
            log.log(Level.SEVERE, "unitDestroyed " + t);
        }

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
    public int enemyEnterLOS(Unit enemy) {
        return 0;
    }

    @Override
    public int enemyLeaveLOS(Unit enemy) {
        return 0; // signaling: OK
    }

    @Override
    public int enemyEnterRadar(Unit enemy) {
        try {
            //TODO handle these events better
            SpringBot.write("enemy spotted: " + enemy.getDef().getName() + "," + enemy.getPos());
            DecisionMaker.get().ThreatMap.addToMap(enemy.getPos(), 1);
        } catch (Throwable t) {
            log.log(Level.SEVERE, "enemyEnterRadar " + t);
        }

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
    public int enemyDestroyed(Unit enemy, Unit attacker) {
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

    private static class MyCustomLogFormatter extends Formatter {

        private DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss:SSS dd.MM.yyyy");

        public String format(LogRecord record) {

            // Create a StringBuffer to contain the formatted record
            // start with the date.
            StringBuffer sb = new StringBuffer();

            // Get the date from the LogRecord and add it to the buffer
            Date date = new Date(record.getMillis());
            sb.append(dateFormat.format(date));
            sb.append("\t");

            // Get the level name and add it to the buffer
            sb.append(record.getLevel().getName());
            sb.append("\t");

            // Get the formatted message (includes localization
            // and substitution of paramters) and add it to the buffer
            sb.append(formatMessage(record));
            sb.append("\r\n");

            return sb.toString();
        }
    }

}