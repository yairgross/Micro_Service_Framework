package bgu.spl.mics.application.services;

import bgu.spl.mics.Callback;
import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;

import javax.smartcardio.TerminalFactory;
import java.awt.*;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Student is responsible for sending the {@link TrainModelEvent},
 * {@link TestModelEvent} and {@link PublishResultsEvent}.
 * In addition, it must sign up for the {@link PublishConferenceBroadcast}.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class StudentService extends MicroService {

    private Student student;

    public StudentService(String name) {
        super(name);
    }

    public StudentService(Student student) {
        super(student.getName());
        this.student = student;
    }

    @Override
    protected void initialize() {
        subscribeBroadcast(TickBroadcast.class, c -> {tickReact();});
        subscribeBroadcast(TerminatorBroadcast.class, c -> {terminate();});
        subscribeBroadcast(PublishConferenceBroadcast.class, c -> {conferenceReact(c);});
    }

    private void tickReact() {
            workOnModels();
    }

    /**
     * Runs every tick, check the {@link Model.Status} of the current{@Link Model} and calls
     * the corresponding {@link} event
     */


    private void workOnModels() {
        Model model = student.getCurrentModel();
        if (!model.isPublished()) {
            if (model.isTested()) {
                if (model.isGood()) {
                    sendEvent(new PublishResultsEvent(model));
                    student.nextModel();
                }
            }
            else if (model.isTrained() && !model.isSentToTesting()) {
                    model.sendToTesting();
                    sendEvent(new TestModelEvent(model));
                }
            else if (model.isTraining()) {}
            else if (model.isPreTrained()) {
                if (!model.isSentToProcessing()) {
                    model.sendToProcessing();
                    sendEvent(new ProcessModelEvent(model));
                }
                else if (model.getData().isProcessed()) {
                    model.sendToTraining();
                    sendEvent(new TrainModelEvent(model));
                }
            }
        }
    }

    /**
     * goes over every {@link Model} which was received by the conference and aggregates the number of
     * papers read and published
     * @param c the {@link PublishConferenceBroadcast} sent by one Conference to which
     *  the {@link Student} is subscribed
     */

    private void conferenceReact(PublishConferenceBroadcast c) {
        LinkedList<Model> models = c.getModels();
        Iterator<Model> iter = models.iterator();
        while(iter.hasNext()) {
            Model current = iter.next();
            if (student.equals(current.getStudent()))
                student.publishResults(current);
            else
                student.readPaper();
        }
    }
}
