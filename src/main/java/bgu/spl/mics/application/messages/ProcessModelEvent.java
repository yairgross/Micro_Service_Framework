package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.Model;

public class ProcessModelEvent implements Event<Model> {

    private Model model;

    public ProcessModelEvent(Model model) {
        this.model = model;
    }

    public Model getModel() {return model;}


}
