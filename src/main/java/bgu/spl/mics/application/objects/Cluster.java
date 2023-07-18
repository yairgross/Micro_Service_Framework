package bgu.spl.mics.application.objects;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Passive object representing the cluster.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Cluster {

	private ConcurrentLinkedDeque<CPU> cpus;
	private ConcurrentLinkedDeque<GPU> gpus;
	private ConcurrentHashMap<Data, ConcurrentLinkedDeque<DataBatch>> disk;
	private Statistics stats;

	private static class ClusterHolder { // Implementing the Cluster as a Thread-safe Singleton
		private static Cluster instance = new Cluster();
	}

	public Cluster(){
		cpus = new ConcurrentLinkedDeque<CPU>();
		gpus = new ConcurrentLinkedDeque<GPU>();
		disk = new ConcurrentHashMap<Data,ConcurrentLinkedDeque<DataBatch>>();
		stats = new Statistics();
	}

	public String getStats() {
		synchronized (stats) {
			return stats.toString();
		}
	}
	
	/**
     * Retrieves the single instance of this class.
     */
	public static Cluster getInstance() {
		return Cluster.ClusterHolder.instance;
	}

	public boolean diskContains(DataBatch db) {
		synchronized (disk) {
			return disk.containsKey(db.getData()) && disk.get(db.getData()).contains(db);
		}
	}

	public void setCpus(ConcurrentLinkedDeque<CPU> cpus) {
		this.cpus = cpus;
	}

	public void setGpus(ConcurrentLinkedDeque<GPU> gpus) {
		this.gpus = gpus;
	}

	public void process(Data data) {
		synchronized (disk){
			if (disk.containsKey(data)) {
				ConcurrentLinkedDeque<DataBatch> batches = disk.get(data);
				for (DataBatch db : batches) {
					CPU availableCPU = findAvailableCPU();
					if (availableCPU != null) {
						availableCPU.addBatch(db);
					}
				}
				disk.remove(data);
			}
		}
	}

	private CPU findAvailableCPU() {
		synchronized (cpus) {
			CPU retCPU = cpus.getFirst();
			int minNumOfBatches = retCPU.getNumberOfBatches();
			Iterator<CPU> iter = cpus.iterator();
			while (iter.hasNext()) {
				CPU current = iter.next();
				if (current.getNumberOfBatches() < minNumOfBatches)
					retCPU = current;
			}
			return retCPU;
		}
	}

	/**
	 * Adds a new {@link LinkedList} of {@link DataBatch}s to the {@link Cluster}'s {@code disk}
	 * @param batches the {@link DataBatch}s to be added to the {@code disk}
	 */
	public void addToDisk(ConcurrentLinkedDeque<DataBatch> batches) {
		synchronized (disk) {
			Data data = batches.peekFirst().getData();
			if (!disk.containsKey(data))
				disk.put(data, batches);
		}
	}

	/**
	 * Deletes a given {@link DataBatch} from the {@link Cluster}'s {@code disk}
	 * @param db the {@link DataBatch} to be removed
	 */
	public void removeFromDisk(DataBatch db) {
		synchronized (disk) {
			if (db != null && diskContains(db)) {
				disk.get(db.getData()).remove(db);
			}
		}
	}


	public void updateStats(int batches, int cpuTime, int gpuTime) {
		synchronized (stats) {
			stats.incBatchesProcessed(batches);
			stats.incCPUTimeUsed(cpuTime);
			stats.incGPUTimeUsed(gpuTime);
		}
	}


	/**
	 * Represents the statistics recorded by the {@link Cluster}
	 */
	private class Statistics {

		private LinkedList<Model> modelsTrained;
		int batchesPrecessed = 0;
		int cpuTimeUsed = 0; // in milliseconds
		int gpuTimeUsed = 0; // in milliseconds

		public Statistics() {modelsTrained = new LinkedList<Model>();}

		public int getBatchesPrecessed() {return batchesPrecessed;}

		public int getCpuTimeUsed() {return cpuTimeUsed;}

		public int getGpuTimeUsed() {return gpuTimeUsed;}

		public void addModel(Model model) {modelsTrained.addLast(model);}

		public void incBatchesProcessed(int batchesPrecessed) {this.batchesPrecessed += batchesPrecessed;}

		public void incCPUTimeUsed(int cpuTimeUsed) {this.cpuTimeUsed += cpuTimeUsed;}

		public void incGPUTimeUsed(int gpuTimeUsed) {this.gpuTimeUsed += gpuTimeUsed;}
		
		public String toString() {
			String s = "";
			s = s + "GPU time used: " + getGpuTimeUsed() + " milliseconds.\n";
			s = s + "CPU time used: " + getCpuTimeUsed() + " milliseconds.\n";
			s = s + "Amount of batches processed by the CPUs: " + getBatchesPrecessed() + " batches.\n";
			return s;
		}
	}
}
