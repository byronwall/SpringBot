package BotBrains;

import com.springrts.ai.oo.AIFloat3;

import java.util.Random;

/**
 * Created by byronandanne on 11/26/2014.
 */
public class Util {
    public static float calcDist(AIFloat3 a, AIFloat3 b) {
        float xDistance = a.x - b.x;
        float yDistance = a.y - b.y;
        float zDistance = a.z - b.z;
        float totalDistanceSquared = xDistance * xDistance + yDistance * yDistance + zDistance * zDistance;
        return totalDistanceSquared;
    }

    public static boolean PosIsNull(AIFloat3 pos){

        boolean val =  pos.dot(pos) == 1;

        SpringBot.write("null test:" + pos + "," + val);

        return val;
    }

    public static float clamp(float value, float min, float max){
        return Math.min(Math.max(value, min), max);
    }

    public static AIFloat3 EMPTY_POS = new AIFloat3(-1,-0,-0);

    public static Random RAND = new Random();

    // Move states
    public static final int MOVE_STATE_HOLD_POSITION    = 0;
    public static final int MOVE_STATE_MANEUVER         = 1;
    public static final int MOVE_STATE_ROAM             = 2;

    // Building facing directions
    public static final int FACING_NORTH    = 0;
    public static final int FACING_EAST     = 1;
    public static final int FACING_SOUTH    = 2;
    public static final int FACING_WEST     = 3;

    // Bits for the option field of a command
    public static final int META_KEY        = (1 << 2); // 4
    public static final int DONT_REPEAT     = (1 << 3); // 8
    public static final int RIGHT_MOUSE_KEY = (1 << 4); // 16
    public static final int SHIFT_KEY       = (1 << 5); // 32
    public static final int CONTROL_KEY     = (1 << 6); // 64
    public static final int ALT_KEY         = (1 << 7); // 128

    // Option definitions
    public static final short OPT_NONE = 0;
    public static final short OPT_QUEUE = SHIFT_KEY;
}