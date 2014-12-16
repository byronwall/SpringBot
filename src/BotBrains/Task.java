package BotBrains;

public abstract class Task {
    public boolean delete;
    protected int frameToRunBy;

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
