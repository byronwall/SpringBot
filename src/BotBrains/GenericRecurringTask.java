package BotBrains;

public class GenericRecurringTask extends Task {

    JustRunIt method;
    int frequency;
    int offset;

    public GenericRecurringTask(int frequency, JustRunIt method) {

        this.method = method;
        this.frequency = frequency;
    }

    public GenericRecurringTask(int frequency, int offset, JustRunIt method) {
        this.method = method;
        this.frequency = frequency;
        this.offset = offset;
    }

    @Override
    public boolean run(int frame) {
        method.run(frame);

        return false;
    }

    @Override
    public boolean shouldRun(int frame) {
        return frame % (frequency + offset) == 0;
    }
}
