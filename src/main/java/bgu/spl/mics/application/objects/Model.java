package bgu.spl.mics.application.objects;

import jdk.nashorn.internal.runtime.regexp.joni.constants.TargetInfo;

/**
 * Passive object representing a Deep Learning model.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Model {


    enum Status {PreTrained, Training, Trained, Tested}
    enum Results {None, Good, Bad}

    private String name;
    private Data data;
    private Student student;
    private int trained = 0;
    private Status status;
    private Results results;
    private boolean isPublished = false;
    private boolean isSentToTraining = false;
    private boolean isSentToProcessing = false;
    private boolean isSentToTesting = false;

    /**
     * Constructs a new untrained Model instance
     * @param name the name of the model
     * @param data the {@link Data} the model should be trained on
     * @param student the {@link Student} who created the model
     */
    public Model(String name, Data data, Student student) {
        this.name = name;
        this.data = data;
        this.student = student;
        status = Status.PreTrained;
        results = Results.None;
    }

    public String getName() {return name;}

    public Data getData() {return data;}

    public Student getStudent() {return student;}

    public Status getStatus() {return status;}

    public boolean isPreTrained() { return status == Status.PreTrained;}

    public boolean isTraining() {return status == Status.Training;}

    public boolean isTrained() {return status == Status.Trained;}

    public boolean isTested() {return status == Status.Tested;}

    public boolean isSentToTraining() {return isSentToTraining;}

    public boolean isSentToProcessing() { return isSentToProcessing;}

    public boolean isSentToTesting() {return isSentToTesting;}

    public void sendToTesting() {this.isSentToTesting = true;}

    public void sendToProcessing() { this.isSentToProcessing = true;}

    public boolean isFullyTrained() {return trained >= data.getSize();}

    public Results getResults() {return results;}

    public boolean isGood() {return results== Results.Good;}

    public boolean isPublished() {return isPublished;}

    public void train() {
        trained += 1000;
        if (trained >= data.getSize()) {
            this.status = Status.Trained;
            trained = data.getSize();
        }
    }

    public void setTraining() {status = Status.Training;}

    public void setTrained() {status = Status.Trained;}

    public void setTested() {status = Status.Tested;}

    public void sendToTraining() {isSentToTraining = true;}

    public void setResults(Results results) {this.results = results;}

    public void publish() {isPublished = true;}

    public String toString(){
        String output =  "        Name: "+name+"\n";
        output += "        Data:\n" + data.toString();
        return output;


    }
}
