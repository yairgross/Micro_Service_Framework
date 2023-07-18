package bgu.spl.mics;

import java.util.concurrent.TimeUnit;

/**
 * A Future object represents a promised result - an object that will
 * eventually be resolved to hold a result of some operation. The class allows
 * Retrieving the result once it is available.
 * 
 * Only private methods may be added to this class.
 * No public constructor is allowed except for the empty constructor.
 * @inv (!isDone() && get() == null) || (isDone() && get() != null)
 */
public class Future<T> {

	private T result;
	private boolean isDone;
	private Object lock = new Object();
	
	/**
	 * This should be the the only public constructor in this class.
	 */
	public Future() {
		result = null;
		isDone = false;
	}
	
	/**
     * retrieves the result the Future object holds if it has been resolved.
     * This is a blocking method! It waits for the computation in case it has
     * not been completed.
     * <p>
	 * @pre None
	 * @post isDone() && get() != null && get().getClass() == T
     * @return return the result of type T if it is available, if not wait until it is available.
     */
	public T get() {
		synchronized (lock) {
			while(!isDone()){
				try {
					lock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			lock.notifyAll();
			}
			return result;
		}
	}
	
	/**
     * Resolves the result of this Future object.
	 * @pre !isDone() && get() == null
	 * @post isDone() && get() != null
     */
	public void resolve (T result) {
		if (!isDone() && result != null && this.result == null) {
			synchronized (lock) {
				lock.notifyAll();
				this.result = result;
				isDone = true;
			}
		}
	}
	
	/**
     * @return true if this object has been resolved, false otherwise
     */
	public boolean isDone() {
		return isDone;
	}
	
	/**
     * retrieves the result the Future object holds if it has been resolved,
     * This method is non-blocking, it has a limited amount of time determined
     * by {@code timeout}
     * <p>
	 * @pre timeout > 0
	 * @post isDone() && get() != null && get().getClass() == T
     * @param timeout 	the maximal amount of time units to wait for the result.
     * @param unit		the {@link TimeUnit} time units to wait.
     * @return return the result of type T if it is available, if not, 
     * 	       wait for {@code timeout} TimeUnits {@code unit}. If time has
     *         elapsed, return null.
     */
	public synchronized T get(long timeout, TimeUnit unit) {
		long timeWaited = 0;
		long milTimeout = unit.toMillis(timeout);
		try {
			while (!isDone() && timeWaited <= milTimeout) {
				Thread.sleep(1000);
				timeWaited += 1000;
			}
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		return result;
	}

}
