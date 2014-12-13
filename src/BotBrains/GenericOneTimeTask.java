package BotBrains;

public class GenericOneTimeTask extends Task {

    JustRunIt method;

    public GenericOneTimeTask(int frame, JustRunIt method) {
        this.frameToRunBy = frame;
        this.method = method;
    }

    @Override
    public boolean run(int frame) {
        method.run(frame);

        return true;
    }
}
