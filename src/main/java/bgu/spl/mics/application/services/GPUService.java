package bgu.spl.mics.application.services;

import bgu.spl.mics.Event;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.GPU;
import bgu.spl.mics.application.objects.Model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * GPU service is responsible for handling the
 * {@link TrainModelEvent} and {@link TestModelEvent},
 * in addition to sending the DataPreProcessEvent.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class GPUService extends MicroService {

    private GPU gpu;
    private ConcurrentLinkedQueue<TrainModelEvent> events = new ConcurrentLinkedQueue<TrainModelEvent>();

    public GPUService(String name) {
        super(name);
    }

    public GPUService(String name, GPU gpu){
        super(name);
        this.gpu = gpu;
    }


    @Override
    protected void initialize() {
        subscribeBroadcast(TickBroadcast.class, c -> {tickReact();});
        subscribeBroadcast(TerminatorBroadcast.class, c -> {terminateReact();});
        subscribeEvent(ProcessModelEvent.class, c -> {processModelReact(c);});
        subscribeEvent(TrainModelEvent.class, c -> {trainModelReact(c);});
        subscribeEvent(TestModelEvent.class, c -> {testModelReact(c);});
    }
    
    private void tickReact() {
        gpu.tick();
    }

    private void terminateReact() {
        terminate();
    }


    private void processModelReact(ProcessModelEvent event) {
        Model model = event.getModel();
        gpu.startProcessing(model);
    }

    private void trainModelReact(TrainModelEvent event) {
        Model model = event.getModel();
        events.add(event);
        model.sendToTraining();
        gpu.startTraining(model);
    }

    private void testModelReact(TestModelEvent event) {
        Model model = event.getModel();
        gpu.testModel(model);
    }
}
