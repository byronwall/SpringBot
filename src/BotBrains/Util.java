package BotBrains;

import com.springrts.ai.oo.AIFloat3;
import com.springrts.ai.oo.clb.UnitDef;
import com.springrts.ai.oo.clb.WeaponMount;

import java.util.Random;

/**
 * Created by byronandanne on 11/26/2014.
 */
public class Util {
    // Move states
    public static final int MOVE_STATE_HOLD_POSITION = 0;
    public static final int MOVE_STATE_MANEUVER = 1;
    public static final int MOVE_STATE_ROAM = 2;
    // Building facing directions
    public static final int FACING_NORTH = 0;
    public static final int FACING_EAST = 1;
    public static final int FACING_SOUTH = 2;
    public static final int FACING_WEST = 3;
    // Bits for the option field of a command
    public static final int META_KEY = (1 << 2); // 4
    public static final int DONT_REPEAT = (1 << 3); // 8
    public static final int RIGHT_MOUSE_KEY = (1 << 4); // 16
    public static final int SHIFT_KEY = (1 << 5); // 32
    public static final int CONTROL_KEY = (1 << 6); // 64
    public static final int ALT_KEY = (1 << 7); // 128
    // Option definitions
    public static final short OPT_NONE = 0;
    public static final short OPT_QUEUE = SHIFT_KEY;
    public static AIFloat3 EMPTY_POS = new AIFloat3(-1, -0, -0);
    public static Random RAND = new Random();

    public static float calcDist(AIFloat3 a, AIFloat3 b) {
        float xDistance = a.x - b.x;
        float yDistance = a.y - b.y;
        float zDistance = a.z - b.z;
        float totalDistanceSquared = xDistance * xDistance + yDistance * yDistance + zDistance * zDistance;
        return totalDistanceSquared;
    }

    public static boolean PosIsNull(AIFloat3 pos){

        return pos.dot(pos) == 1;
    }

    public static AIFloat3 getRandomNearbyPosition(AIFloat3 pos, float distance) {

        float z_delta = Util.RAND.nextFloat() * distance - distance / 2;
        float x_delta = Util.RAND.nextFloat() * distance - distance / 2;

        return new AIFloat3(pos.x + x_delta, pos.y, pos.z + z_delta);
    }

    public static float getTotalDamage(UnitDef def) {
        float damage = 0;

        for (WeaponMount weaponMount : def.getWeaponMounts()) {
            if (weaponMount.getWeaponDef().isAbleToAttackGround() && !weaponMount.getWeaponDef().isFireSubmersed()) {
                damage += weaponMount.getWeaponDef().getDamage().getTypes().get(0) * weaponMount.getWeaponDef().getAreaOfEffect() * weaponMount.getWeaponDef().getSalvoSize();
            }
        }
        return damage;
    }

    public static float clamp(float value, float min, float max) {
        return Math.min(Math.max(value, min), max);
    }

    public static float getMetalMake(UnitDef def) {
        //TODO determine why these things are not turnign on...
        if (def.getName().equals("armmakr") || def.getName().equals("armmmkr") || def.getName().equals("cormakr") || def.getName().equals("cormmkr")) {
            return 0.000666f;
        } else {
            return 0;
        }
    }

    public static float getSolarSuck(UnitDef def) {
        if (def.getName().equals("armmakr") || def.getName().equals("armmmkr") || def.getName().equals("cormakr") || def.getName().equals("cormmkr")) {
            return 20;
        } else {
            return 0;
        }
    }
}
