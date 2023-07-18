package bgu.spl.mics.application.objects;

/**
 *  Represents a {@link DataBatch} instance with a destined time in which it will be ready to use (for some purpose)
 */
public class TimedBatch {

    private DataBatch batch;
    private int readyTime;

    public TimedBatch(DataBatch batch, int readyTime) {
        this.batch = batch;
        this.readyTime = readyTime;
    }

    public DataBatch getBatch() {
        return batch;
    }

    public int getReadyTime() {
        return readyTime;
    }
}