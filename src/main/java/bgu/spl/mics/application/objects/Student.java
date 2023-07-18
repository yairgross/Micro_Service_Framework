package bgu.spl.mics.application.objects;

import com.sun.org.apache.xpath.internal.operations.Mod;
import sun.awt.image.ImageWatched;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Passive object representing single student.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Student {
    /**
     * Enum representing the Degree the student is studying for.
     */
    enum Degree {
        MSc, PhD
    }

    private String name;
    private String department;
    private Degree status;
    private ConcurrentLinkedDeque<Model> models = new ConcurrentLinkedDeque<Model>();
    private int publications = 0;
    private int papersRead = 0;

    /**
     * Constructs a "fresh" (haven't yet read or published any papers) student of a specified degree
     * @param name name of the student
     * @param department the department the student belongs to
     * @param status what degree this student is pursuing
     */
    public Student(String name, String department, Degree status) {
        this.name = name;
        this.department = department;
        this.status = status;
    }

    public Student(String name, String department, String status) {
        this.name = name;
        this.department = department;
        if (status.equals("MSc"))
            this.status = Degree.MSc;
        else
            this.status = Degree.PhD;
    }

    public String getName() {return name;}

    public String getDepartment() {return department;}

    public Degree getStatus() {return status;}

    public Model getCurrentModel() {return models.getFirst();}

    public void nextModel() {
        if (!models.isEmpty()) {
            models.addLast(models.removeFirst());
        }
    }

    public void setModels(ConcurrentLinkedDeque<Model> models) {
        if (models != null) {
            this.models = models;
        }
    }

    public int getPublications() {return publications;}

    public int getPapersRead() {return papersRead;}

    public void publishResults(Model model) {
        if (model != null && models.contains(model)) {
            publications++;
            model.publish();
        }
    }

    public void readPaper() {papersRead++;}

    public String toString() {
        String s = "";
        s += "    Name: " + name + "\n";
        s += "    Department: " + department + "\n";
        s += "    Status: " + statusToString() + "\n";
        s += "    Number of publications: " + publications + "\n";
        s += "    Number of papers read: " + papersRead + "\n";
        s += "    Models trained: " + "\n";
        for (Model model : models) {
            if (model.isTrained() || model.isTested()) {
                s += model.toString();
                if (model.isPublished()) {
                    s += "        This model was published\n";
                }
            }
        }
        return s;
    }

    private String statusToString() {
        if (status == Degree.MSc)
            return "MSc";
        return "PhD";
    }
}
