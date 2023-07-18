package bgu.spl.mics.application.objects;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Passive object representing a single GPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 * @inv type != null && cluster != null && (0 <= this.getVramSize() < vram.length) && (0 <= this.getDiskSize())
 */
public class GPU {
    /**
     * Enum representing the type of the GPU.
     */
    enum Type {RTX3090, RTX2080, GTX1080}

    private Type type;
    private Cluster cluster;
    private VRAM vram;
    private int timer = 0;
    private Model currentModel = null;
    private int currentTrainingBatchIndex = 0;

    public GPU(Type type, Cluster cluster) {
        this.type = type;
        this.cluster = cluster;
        int VramCapacity = 0;
        switch (type) {
            case RTX3090: VramCapacity = 32;
            case RTX2080: VramCapacity = 16;
            case GTX1080: VramCapacity = 8;
        }
        vram = new VRAM(VramCapacity);
    }

    public GPU(String type, Cluster cluster) {
        this.type = stringToType(type);
        this.cluster = cluster;
        int VramCapacity = 0;
        switch (this.type) {
            case RTX3090: VramCapacity = 32;
            case RTX2080: VramCapacity = 16;
            case GTX1080: VramCapacity = 8;
        }
        vram = new VRAM(VramCapacity);
    }

    private Type stringToType(String type) {
        Type ret = Type.RTX3090;
        if (type.equals("RTX2080"))
            ret = Type.RTX2080;
        else if (type.equals("GTX1080"))
            ret = Type.GTX1080;
        return ret;
    }

    /**
     * Advance the timer by one tick
     */
    public void tick() {
        timer++;
        checkTrained();
    }

    /**
     * @return the {@link GPU}'s {@link Data.Type}
     */
    public Type getType() {return type;}

    /**
     * @return the {@link GPU}'s {@link Cluster}
     */
    public Cluster getCluster() {return cluster;}

    /**
     * Transfers a given {@link DataBatch} from the {@link Cluster} to the {@link GPU}'s {@code vram}
     * @param db - the {@link DataBatch} to be transferred
     */
    public void diskToVRAM(DataBatch db) {
        if (db != null && cluster.diskContains(db)) {
            if (!vram.isFull() && !vram.contains(db)) {
                cluster.removeFromDisk(db);
                vram.add(db);
            }
        }
    }

    /**
     * Sends the {@link Data} of {@param model} to precessing in the {@link Cluster}
     */
    public void startProcessing(Model model) {
        Data data = model.getData();
        ConcurrentLinkedDeque<DataBatch> batches = data.toBatches();
        cluster.addToDisk(batches);
        cluster.process(data);
    }

    /**
     * Initiates the training of {@param model} in {@cod this} {@link GPU}
     */
    public void startTraining(Model model) {
        model.setTraining();
        currentModel = model;
    }

    /**
     * Check if a {@link Model} finished its training and acts accordingly
     */
    public void checkTrained() {
        if (currentModel != null) {
            if (currentTrainingBatchIndex >= currentModel.getData().toBatches().size()-1) {
                currentModel.setTrained();
                cluster.updateStats(0,0,timeToTrain(currentModel));
                currentModel = null;
                currentTrainingBatchIndex = 0;
            }
            else {
                TrainDataBatches();
            }
        }
    }


    /**
     * Trains the {@code currentModel} on the {@link DataBatch}s in the {@link VRAM}
     */
    public void TrainDataBatches(){
        while (!vram.isEmpty() && vram.getFirst().getReadyTime() <= timer) {
            currentModel.train();
            currentTrainingBatchIndex++;
            vram.removeFirst();
        }
        while (!vram.isFull() && currentTrainingBatchIndex < currentModel.getData().toBatches().size()) {
            Iterator<DataBatch> iter = currentModel.getData().toBatches().iterator();
            int iterIndex = 0;
            DataBatch toTrain = null;
            while (iter.hasNext() && toTrain == null) {
                if (iterIndex == currentTrainingBatchIndex)
                    toTrain = iter.next();
                iterIndex++;
            }
            vram.add(toTrain);
            currentTrainingBatchIndex++;
        }
    }

    /**
     * Calculates the time this {@link GPU} needs to train a {@link Model} on a single {@link DataBatch}
     * @return how many ticks the training will take
     */
    public int timeToTrain() {
        int time = -1;
        switch (type) {
            case RTX3090: time = 1;
            case RTX2080: time = 2;
            case GTX1080: time = 4;
        }
        return time;
    }

    /**
     * Calculates the time needed for the {@link GPU} to train a {@link Model} on all its {@link Data}
     * @param model the {@link Model} to be trained
     * @return how many ticks the training will take
     */
    public int timeToTrain(Model model) {
        return timeToTrain() * model.getData().toBatches().size();
    }


    /**
     * Tests a given {@link Model} according to the degree of the relevant {@link Student}
     * @pre model.getResults() != Good
     * @post model.getStatus() == Tested
     * @param model the model to be tested
     * @return true in prob. of 0.1 for {@code MSc} and in prob. of 0.2 for {@code Phd}
     */
    public void testModel(Model model) {
        model.setTested();
        Model.Results results = Model.Results.Bad;
        Student.Degree degree = model.getStudent().getStatus();
        double r = Math.random();
        switch (degree) {
            case MSc:
                if (r < 0.6) results = Model.Results.Good;
            case PhD:
                if (r < 0.8) results = Model.Results.Good;
        }
        model.setResults(results);
    }

    /**
     * A class representing a {@link GPU}'s VRAM
     */
    private class VRAM {

        private int capacity;
        LinkedList<TimedBatch> batches;

        public VRAM(int capacity) {
            this.capacity = capacity;
            batches = new LinkedList<TimedBatch>();
        }

        public int getSize() {return batches.size();}

        public boolean isEmpty() {return batches.isEmpty();}

        public boolean isFull() {return batches.size() == capacity;}

        public boolean contains(DataBatch db) {return batches.contains(db);}

        public void add(DataBatch db) {
            if (isFull())
                throw new IndexOutOfBoundsException("This GPU's VRAM is currently full");
            int timeToTrain = timeToTrain();
            if (isEmpty())
                batches.addLast(new TimedBatch(db, timer + timeToTrain));
            else
                batches.addLast(new TimedBatch(db, getLast().getReadyTime() + timeToTrain));

        }

        public void remove(DataBatch db) {
            if (contains(db)) {
                batches.remove(db);
            }
        }

        public TimedBatch getFirst() {
            if (!isEmpty())
                return batches.getFirst();
            return null;
        }

        public TimedBatch getLast() {
            if (!isEmpty())
                return batches.getLast();
            return null;
        }

        public void removeFirst() {
            if (!isEmpty())
                batches.removeFirst();
        }

    }
}
