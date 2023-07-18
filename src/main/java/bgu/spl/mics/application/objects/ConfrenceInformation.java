package bgu.spl.mics.application.objects;

//import javax.jws.WebParam;
import java.util.LinkedList;

/**
 * Passive object representing information on a conference.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class ConfrenceInformation {

    private String name;
    private int date;
    private LinkedList<Model> models = new LinkedList<Model>();

    public ConfrenceInformation(String name, int date){
        this.name = name;
        this.date = date;
    }

    public String getName() { return name; }

    public int getDate() { return date; }

    public void addModel(Model model){
        if(model != null)
            models.addLast(model);
    }

    public LinkedList<Model> getModels() { return models; }

    public String toString(){
        String output = "    Name: " + name + "\n";
        output += "    Date: " + date + "\n";
        output += "    Publications: \n";
        for(Model model : models){
            output += model.toString();
            output += "    \n";
        }
        return output;
    }

}
