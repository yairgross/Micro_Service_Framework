package bgu.spl.mics;

import java.util.Iterator;
import java.util.concurrent.*;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {

	private ConcurrentHashMap<MicroService,ConcurrentLinkedQueue<Message>> services;
	private ConcurrentHashMap<Class<? extends Event>,ConcurrentLinkedDeque<MicroService>> events;
	private ConcurrentHashMap<Class<? extends Broadcast>,ConcurrentLinkedDeque<MicroService>> broadcasts;
	private ConcurrentHashMap<Event,Future> futures;


	private static class BusHolder { // Implementing the Message Bus as a Thread-safe Singleton
		private static MessageBusImpl instance = new MessageBusImpl();
	}

	public MessageBusImpl(){
		services = new ConcurrentHashMap<MicroService,ConcurrentLinkedQueue<Message>>();
		events = new ConcurrentHashMap<Class<? extends Event>,ConcurrentLinkedDeque<MicroService>>();
		broadcasts = new ConcurrentHashMap<Class<? extends Broadcast>,ConcurrentLinkedDeque<MicroService>>();
		futures = new ConcurrentHashMap<Event,Future>();
	}

	public static MessageBusImpl getInstance() {
		return BusHolder.instance;
	}

	public boolean isRegistered(MicroService ms) {
		return services.containsKey(ms);
	}

	@Override
	public <T> boolean isSubEvent(Class<? extends Event<T>> type, MicroService m) {
		return events.containsKey(type) && events.get(type).contains(m);
	}

	@Override
	public boolean isSubBroadcast(Class<? extends Broadcast> type, MicroService m) {
		return broadcasts.containsKey(type) && broadcasts.get(type).contains(m);
	}
	
	@Override
	public <T> Future<T> getFuture(Event<T> e) {
		if (futures.containsKey(e))
			return (Future<T>)futures.get(e);
		return null;
	}

	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		if (!isSubEvent(type,m)) {
			if (events.containsKey(type)) {
				events.get(type).addLast(m);
			}
			else {
				ConcurrentLinkedDeque<MicroService> subs = new ConcurrentLinkedDeque<MicroService>();
				subs.addLast(m);
				events.put(type,subs);
			}
		}
	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		if (!isSubBroadcast(type,m)) {
			if (broadcasts.containsKey(type)) {
				broadcasts.get(type).addLast(m);
			}
			else {
				ConcurrentLinkedDeque<MicroService> subs = new ConcurrentLinkedDeque<>();
				subs.addLast(m);
				broadcasts.put(type,subs);
			}
		}
	}

	@Override
	public <T> void complete(Event<T> e, T result) {
		if (futures.containsKey(e)) {
			Future<T> future = futures.get(e);
			future.resolve(result);
		}
	}

	@Override
	public void sendBroadcast(Broadcast b) {
		if (broadcasts.containsKey(b.getClass())) {
			ConcurrentLinkedDeque<MicroService> subs = broadcasts.get(b.getClass());
			synchronized (subs) {
				Iterator<MicroService> iter = subs.iterator();
				while (iter.hasNext()) {
					MicroService current = iter.next();
					ConcurrentLinkedQueue<Message> q = services.get(current);
					if (q != null) {
						synchronized (q) {
							q.add(b);
							q.notifyAll();
						}
					}
				}
			}
		}
	}

	
	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		Future<T> f = new Future<T>();
		if (events.containsKey(e.getClass())) {
			ConcurrentLinkedDeque<MicroService> subs = events.get(e.getClass());
			MicroService ms = subs.pollFirst();
			synchronized (subs) {
				if (ms != null) {
					ConcurrentLinkedQueue<Message> q = services.get(ms);
					synchronized (q) {
						q.add(e);
						q.notifyAll();
					}
					subs.addLast(ms);
					futures.put(e, f);
				}
			}
		}
		return f;
	}

	@Override
	public void register(MicroService m) {
			if (!isRegistered(m))
				services.put(m, new ConcurrentLinkedQueue<Message>());
	}

	@Override
	public void unregister(MicroService m) {
		if(isRegistered(m)) {
			services.remove(m);
			Iterator<Class<? extends Event>> eventsIter = events.keySet().iterator();
			while (eventsIter.hasNext()) {
				Class<? extends Event> current = eventsIter.next();
				events.get(current).remove(m);
			}
			Iterator<Class<? extends Broadcast>> broadcastsIter = broadcasts.keySet().iterator();
			while (broadcastsIter.hasNext()) {
				Class<? extends Broadcast> current = broadcastsIter.next();
				broadcasts.get(current).remove(m);
			}
		}
	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		if (!isRegistered(m))
			throw new IllegalStateException("MicroService '" + m.getName() + "' is not registered to the link MessageBus");
		ConcurrentLinkedQueue<Message> q = services.get(m);
		synchronized (q) {
			while (q.isEmpty()) {
				q.wait();
			}
			q.notifyAll();
			return q.remove();
		}
	}

}
