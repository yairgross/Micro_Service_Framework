package bgu.spl.mics.application.services;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.Callback;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;

import javax.security.auth.kerberos.KerberosTicket;
import java.util.Timer;

/**
 * TimeService is the global system timer There is only one instance of this micro-service.
 * It keeps track of the amount of ticks passed since initialization and notifies
 * all other micro-services about the current time tick using {@link TickBroadcast}.
 * This class may not hold references for objects which it is not responsible for.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class TimeService extends MicroService{

	private int speed; // The time each tick takes in milliseconds
	private int duration; // The number of ticks before termination
	private int counter = 1;

	public TimeService(int speed, int duration){
		super("Time Service");
		this.speed = speed;
		this.duration = duration;
	}

	public TimeService() {
		super("Time Service");
		this.speed = 1000;
		this.duration = 50;
	}

	public boolean isTimeOver() {return counter >= duration;}

	@Override
	protected void initialize() {
		subscribeBroadcast(TickBroadcast.class, c -> {tickReact();});
		sendBroadcast(new TickBroadcast(counter));
	}

	private void tickReact() {
		if (!isTimeOver()) {
			try {
				Thread.sleep(speed);
				counter++;
				sendBroadcast(new TickBroadcast(counter));
			}
			catch (InterruptedException exc) {
			}
		}
		else {
			sendBroadcast(new TerminatorBroadcast());
			terminate();
		}
	}

}
