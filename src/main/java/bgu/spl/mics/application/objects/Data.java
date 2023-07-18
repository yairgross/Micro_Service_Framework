package bgu.spl.mics.application.objects;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Passive object representing a data used by a model.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Data {
    /**
     * Enum representing the Data type.
     */
    enum Type {
        Images, Text, Tabular
    }

    private Type type;
    private int processed = 0;
    private int size;


    public Data(Type type, int size) {
        this.type = type;
        this.size = size;
    }

    public Data(String type, int size) {
        this.type = Type.Images;
        if (type.equals("Text"))
            this.type = Type.Text;
        else if (type.equals("Tabular"))
            this.type = Type.Tabular;
        this.size = size;
    }

    public Type getType() {return type;}

    public boolean isProcessed() {return processed == size;}

    public int getSize() {return size;}

    /**
     * Processes a given amount of samples in the {@link Data} instance
     * @param samples the amount of samples to process
     */
    public synchronized void process(int samples){
        if(!isProcessed()) {
            processed += samples;
            if (processed > size) {
                processed = size;
            }
        }
    }

    /**
     * Converts the {@link Data} instance into multiple {@link DataBatch} objects
     * @return a ConcurrentLinkedDeque of {@link DataBatch}s derived from this {@link Data}
     */
    public ConcurrentLinkedDeque<DataBatch> toBatches() {
        ConcurrentLinkedDeque<DataBatch> batches = new ConcurrentLinkedDeque<DataBatch>();
        for (int i = 0; i < (size/1000); i++) {
            batches.addLast(new DataBatch(this, 1000*i));
        }
        return batches;
    }

    /**
     * toString method used for generating the output file
     */
    public String toString(){
        String output = "            Type: " + typeToString() +"\n";
        output += "            Size: " + size + "\n";
        return output;
    }

    public String typeToString(){
        if(type == Type.Images)
            return "Images";
        else if(type == Type.Text)
            return "Text";
        return "Tabular";
    }
}
