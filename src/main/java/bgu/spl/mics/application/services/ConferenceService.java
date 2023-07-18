package bgu.spl.mics.application.services;

import bgu.spl.mics.Callback;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.PublishConferenceBroadcast;
import bgu.spl.mics.application.messages.PublishResultsEvent;
import bgu.spl.mics.application.messages.TerminatorBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.ConfrenceInformation;
import bgu.spl.mics.application.objects.Model;

import java.util.LinkedList;

/**
 * Conference service is in charge of
 * aggregating good results and publishing them via the {@link PublishConferenceBroadcast},
 * after publishing results the conference will unregister from the system.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class ConferenceService extends MicroService {

    private ConfrenceInformation conf;

    public ConferenceService(String name) {
        super(name);
    }

    public ConferenceService(ConfrenceInformation conf){
        super(conf.getName());
        this.conf = conf;
    }

    @Override
    protected void initialize() {
        subscribeBroadcast(TickBroadcast.class, c -> {tickReact(c);});
        subscribeBroadcast(TerminatorBroadcast.class, c -> {terminate();});
        subscribeEvent(PublishResultsEvent.class, c -> {publishReact(c);});
    }

    private void tickReact(TickBroadcast b) {
        if (b.getTime() >= conf.getDate()) {
            sendBroadcast(new PublishConferenceBroadcast(conf.getModels()));
            terminate();
        }
    }

    private void publishReact(PublishResultsEvent event) {
        Model model = event.getModel();
        if (model.isGood()) {
            conf.addModel(model);
            model.publish();
        }

    }

}
