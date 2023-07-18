package bgu.spl.mics;

/**
 * The message-bus is a shared object used for communication between
 * micro-services.
 * It should be implemented as a thread-safe singleton.
 * The message-bus implementation must be thread-safe as
 * it is shared between all the micro-services in the system.
 * You must not alter any of the given methods of this interface. 
 * You cannot add methods to this interface.
 */
public interface MessageBus {

    /**
     * Checks whether a given {@link MicroService} is registered in this {@link MessageBusImpl}
     * @pre ms != null
     * @param ms the {@link MicroService} we suspect is registered
     * @return true iff {@code queues} has a MSQ with the specified {@link MicroService}
     */
    boolean isRegistered(MicroService ms);

    /**
     * Checks whether a given {@link MicroService} is subscribed to a given {@link Event class}
     * @pre type, m != null
     * @param type the {@link Event class}
     * @param m the {@link MicroService} instance
     * @return true iff {@cide m} is sunscribed to {@code type} events
     */
    <T> boolean isSubEvent(Class<? extends Event<T>> type, MicroService m);

    /**
     * Checks whether a given {@link MicroService} is subscribed to a given {@link Broadcast class}
     * @pre type, m != null
     * @param type the {@link Broadcast class}
     * @param m the {@link MicroService} instance
     * @return true iff {@cide m} is sunscribed to {@code type} broadcast
     */
    boolean isSubBroadcast(Class<? extends Broadcast> type, MicroService m);

    /**
     * @param e the {@link Event} instance
     * @return the {@link Future} instance associated with the {@link Event} {@code e}
     */
    <T> Future<T> getFuture(Event<T> e);

    /**
     * Subscribes {@code m} to receive {@link Event}s of type {@code type}.
     * <p>
     * @pre type != null && m != null && isRegistered(m)
     * @post isSubEvent(type, m)
     * @param <T>  The type of the result expected by the completed event.
     * @param type The type to subscribe to,
     * @param m    The subscribing micro-service.
     */
    <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m);

    /**
     * Subscribes {@code m} to receive {@link Broadcast}s of type {@code type}.
     * <p>
     * @pre type != null && m != null && isRegistered(m)
     * @post isSubBroadcast(type, m)
     * @param type 	The type to subscribe to.
     * @param m    	The subscribing micro-service.
     */
    void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m);

    /**
     * Notifies the MessageBus that the event {@code e} is completed and its
     * result was {@code result}.
     * When this method is called, the message-bus will resolve the {@link Future}
     * object associated with {@link Event} {@code e}.
     * <p>
     * @pre result != null && !e.getFuture().isDone()
     * @post the
     * @param <T>    The type of the result expected by the completed event.
     * @param e      The completed event.
     * @param result The resolved result of the completed event.
     */
    <T> void complete(Event<T> e, T result);

    /**
     * Adds the {@link Broadcast} {@code b} to the message queues of all the
     * micro-services subscribed to {@code b.getClass()}.
     * <p>
     * @pre {@code b} != null
     * @post for all {@link MicroService} ms s.t. isSubBroadcast(b, ms), ms's message queue in this
     *       {@link MessageBus} contains {@code b} as last added element
     * @param b 	The message to added to the queues.
     */
    void sendBroadcast(Broadcast b);

    /**
     * Adds the {@link Event} {@code e} to the message queue of one of the
     * micro-services subscribed to {@code e.getClass()} in a round-robin
     * fashion. This method should be non-blocking.
     * <p>
     * @pre {@code e} != null
     * @post exists {@link MicroService} ms s.t. isSubEvent(e, ms), ms's message queue in this
     *       {@link MessageBus} contains {@code b} as last added element
     * @param <T>    	The type of the result expected by the event and its corresponding future object.
     * @param e     	The event to add to the queue.
     * @return {@link Future<T>} object to be resolved once the processing is complete,
     * 	       null in case no micro-service has subscribed to {@code e.getClass()}.
     */
    <T> Future<T> sendEvent(Event<T> e);

    /**
     * Allocates a message-queue for the {@link MicroService} {@code m}.
     * <p>
     * @pre !isRegistered(m)
     * @post isRegistered(m)
     * @param m the micro-service to create a queue for.
     */
    void register(MicroService m);

    /**
     * Removes the message queue allocated to {@code m} via the call to
     * {@link #register(bgu.spl.mics.MicroService)} and cleans all references
     * related to {@code m} in this message-bus. If {@code m} was not
     * registered, nothing should happen.
     * <p>
     * @pre isRegistered(m)
     * @post !isRegistered(m)
     * @param m the micro-service to unregister.
     */
    void unregister(MicroService m);

    /**
     * Using this method, a <b>registered</b> micro-service can take message
     * from its allocated queue.
     * This method is blocking meaning that if no messages
     * are available in the micro-service queue it
     * should wait until a message becomes available.
     * The method should throw the {@link IllegalStateException} in the case
     * where {@code m} was never registered.
     * <p>
     * @pre isRegistered(m)
     * @post if ({@pre m's message queue size > 0}), m's queue size = {@pre m's queue size} -1
     *       else, m's queue size = 0
     * @param m The micro-service requesting to take a message from its message
     *          queue.
     * @return The next message in the {@code m}'s queue (blocking).
     * @throws InterruptedException if interrupted while waiting for a message
     *                              to became available.
     */
    Message awaitMessage(MicroService m) throws InterruptedException;
    
}
