package BotBrains;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by byronandanne on 12/7/2014.
 */
public class TaskManager {

    //TODO create a simple way to keep track of tasks
    //TODO need a simple way to add single tasks
    //TODO need a way to do recurring tasks
    //TODO need an entry point to run the tasks

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
        for (Iterator<Task> iterator = tasks.iterator(); iterator.hasNext(); ) {
            Task task = iterator.next();
            if (task.shouldRun(currentFrame)) {

                try {

                    boolean shouldDelete = task.run(currentFrame);

                    // Remove the current element from the iterator and the list.
                    if (shouldDelete) {
                        iterator.remove();
                    }
                } catch (Throwable t) {
                    SpringBot.write("ERROR task run: " + task + ", message: " + t);
                }
            }
        }

        lastFrameSeen = currentFrame;
    }

}

abstract class Task {
    public boolean delete;
    int frameToRunBy;

    public Task(int frameToRunBy) {
        this.frameToRunBy = frameToRunBy;
    }

    public Task() {
    }

    public boolean shouldRun(int frame) {
        return frameToRunBy <= frame;
    }

    public abstract boolean run(int frame);
}

