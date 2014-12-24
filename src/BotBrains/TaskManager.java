package BotBrains;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by byronandanne on 12/7/2014.
 */
public class TaskManager {

    private static TaskManager _instance = null;
    private List<Task> tasks = new ArrayList<>();
    private int lastFrameSeen = 0;

    private TaskManager() {
    }

    public static TaskManager get() {
        if (_instance == null) {
            _instance = new TaskManager();
        }

        return _instance;
    }

    public void addTask(Task task) {
        //assume that small number is meant to be additive
        if (task.frameToRunBy < lastFrameSeen) {
            task.frameToRunBy += lastFrameSeen;
        }
        tasks.add(task);
    }

    public void processTasks(int currentFrame) {
        //using the reverse order iterator in order to delete from the list while processing
        //don't try to use an iterator here... it will not work correctly
        for (int i = tasks.size() - 1; i >= 0; i--) {
            Task task = tasks.get(i);
            if (task.shouldRun(currentFrame)) {

                if (task.run(currentFrame)) {
                    tasks.remove(i);
                }
            }
        }

        lastFrameSeen = currentFrame;
    }

}

