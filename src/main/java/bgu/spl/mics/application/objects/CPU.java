package bgu.spl.mics.application.objects;

import sun.awt.image.ImageWatched;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Passive object representing a single CPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 * @inv getNumberOfCores() > 0 && getNumberOfBatches() >= 0 && getCluster() != null
 */

public class CPU {

    private int cores;
    private ConcurrentLinkedDeque<TimedBatch> data;
    private Cluster cluster;
    private int timer = 0;

    public CPU(int cores, Cluster cluster) {
        this.cores = cores;
        data = new ConcurrentLinkedDeque<>();
        this.cluster = cluster;
    }


    /**
     * @return number of cores in the CPU
     */
    public int getNumberOfCores() {
        return cores;
    }

    /**
     * @return this CPU's associated cluster
     */
    public Cluster getCluster() {
        return cluster;
    }

    /**
     * @return number of data batches currently in the CPU
     */
    public int getNumberOfBatches() {
        return data.size();
    }


    /**
     * Confirms a given {@link DataBatch} instance is in this CPU
     * @param db the {@link DataBatch} instance we suspect is in this CPU
     * @pre db != null
     * @return true iff db is in the {@code data} field
     */
    public synchronized boolean contains (DataBatch db){
        Iterator<TimedBatch> iter = data.iterator();
        while (iter.hasNext()) {
            TimedBatch current = iter.next();
            if (current.getBatch().equals(db))
                return true;
        }
        return false;
    }

    /**
     * Updates the internal clock of the {@link CPU}
     */
    public synchronized void tick () {
        timer++;
        checkProcessed();
    }

    /**
     * Calculates how much ticks are needed to process a given {@link DataBatch} instance
     * @param db the {@link DataBatch} to be processed
     * @return the number of ticks needed to process {@code db}
     */
    public int getProcessingTime (DataBatch db){
        int time = -1;
        Data.Type type = db.getType();
        switch (type) {
            case Images:
                time = (32 / getNumberOfCores()) * 4;
            case Text:
                time = (32 / getNumberOfCores()) * 2;
            case Tabular:
                time = (32 / getNumberOfCores()) * 1;
        }
        return time;
    }

    /**
     * Adds a new {@link DataBatch} to the CPU's data field
     * @pre db != null
     * @post getNumberOfBatches() == {@pre getNumberOfBatches()} + 1 && contains(db)
     * @param db the {@link DataBatch} sent to the CPU
     */
    public synchronized void addBatch (DataBatch db){
        if (db != null) {
            int lastBatchReadyTime = timer;
            if (!data.isEmpty())
                lastBatchReadyTime = data.getLast().getReadyTime();
            int readyTime = lastBatchReadyTime + getProcessingTime(db);
            TimedBatch newLastBatch = new TimedBatch(db, readyTime);
            data.addLast(newLastBatch);
        }
    }

    /**
     * Checks if the current processing {@link DataBatch} is ready
     * @pre !data.isEmpty() && isReady(data.peekFirst())
     * @post db.getProcessed() == {@pre db.getProcessed()} + {@code samplesPerTick}
     */
    public synchronized void checkProcessed() {
        if (!data.isEmpty()) {
            while (isReady(data.peek())) {
                TimedBatch tb = data.remove();
                tb.getBatch().process();
                cluster.updateStats(1, getProcessingTime(tb.getBatch()), 0);
            }
        }
    }

    /**
     * @param tb the {@link TimedBatch} instance we want to check
     * @return true iff {@code tb} has finished processing
     */
    private synchronized boolean isReady(TimedBatch tb) {
        return tb != null && tb.getReadyTime() <= timer;
    }

}

