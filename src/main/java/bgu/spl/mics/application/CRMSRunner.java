package bgu.spl.mics.application;

import bgu.spl.mics.MessageBus;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.*;
import com.google.gson.*;
import java.io.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;

/** This is the Main class of Compute Resources Management System application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output a text file.
 */
public class CRMSRunner {
    public static void main(String[] args) {
        // Defining the singletons of the CRMS
        Cluster cluster = Cluster.getInstance();
        MessageBus bus = MessageBusImpl.getInstance();
        // Defining empty Objects to be filled with the parameters from the input file
        TimeService timeService = null;
        LinkedList<Student> students = new LinkedList<Student>();
        ConcurrentLinkedDeque<GPU> gpus = new ConcurrentLinkedDeque<GPU>();
        ConcurrentLinkedDeque<CPU> cpus = new ConcurrentLinkedDeque<CPU>();
        LinkedList<ConfrenceInformation> confInfos = new LinkedList<ConfrenceInformation>();
        Integer tickTime = 1;
        Integer duration = 1000;
        // Extracting the parameters from the input .json file into the empty Objects
        File input = new File(args[0]);
        try {
            JsonElement fileElement = JsonParser.parseReader(new FileReader(input));
            JsonObject fileObject = fileElement.getAsJsonObject();
            JsonArray studentsArray = fileObject.get("Students").getAsJsonArray();
            // Extracting the Student objects from the input file
            for (JsonElement studentElement : studentsArray) {
                JsonObject studentObject = studentElement.getAsJsonObject();
                String studentName = studentObject.get("name").getAsString();
                String department = studentObject.get("department").getAsString();
                String status = studentObject.get("status").getAsString();
                ConcurrentLinkedDeque<Model> models = new ConcurrentLinkedDeque<>();
                JsonArray modelsArray = studentObject.get("models").getAsJsonArray();
                Student student = new Student(studentName, department, status);
                for (JsonElement modelElement : modelsArray) {
                    JsonObject modelObject = modelElement.getAsJsonObject();
                    String modelName = modelObject.get("name").getAsString();
                    String type = modelObject.get("type").getAsString();
                    Integer size = modelObject.get("size").getAsInt();
                    Data data = new Data(type, size);
                    models.addLast(new Model(modelName, data, student));
                }
                student.setModels(models);
                students.addLast(student);
            }
            // Extracting the GPU objects from the input file
            JsonArray gpusArray = fileObject.get("GPUS").getAsJsonArray();
            for (JsonElement gpuElement : gpusArray) {
                String type = gpuElement.getAsString();
                GPU gpu = new GPU(type, cluster);
                gpus.addLast(gpu);
            }
            // Extracting the CPU objects from the input file
            JsonArray cpusArray = fileObject.get("CPUS").getAsJsonArray();
            for (JsonElement cpuElement : cpusArray) {
                Integer cores = cpuElement.getAsInt();
                CPU cpu = new CPU(cores, cluster);
                cpus.addLast(cpu);
            }

            // Adding the cpus and gpus to the cluster
            cluster.setCpus(cpus);
            cluster.setGpus(gpus);

            // Extracting the ConferenceInformation objects from the input file
            JsonArray confsArray = fileObject.get("Conferences").getAsJsonArray();
            for (JsonElement confElement : confsArray) {
                JsonObject confObject = confElement.getAsJsonObject();
                String name = confObject.get("name").getAsString();
                Integer date = confObject.get("date").getAsInt();
                ConfrenceInformation conf = new ConfrenceInformation(name, date);
                confInfos.addLast(conf);
            }
            // Extracting the TimeService from the input file
            tickTime = fileObject.get("TickTime").getAsInt();
            duration = fileObject.get("Duration").getAsInt();
            timeService = new TimeService(tickTime, duration);
            bus.register(timeService);
        } catch (FileNotFoundException exc) {
        }

        // Registering the extracted objects as MicroServices to the MessageBus and assigning a new thread for each MicroService

        // Registering and executing each GPU from the gpus list as a new MicroService in the system
        int gpuCounter = 1;
        Iterator<GPU> gpusIter = gpus.iterator();
        while (gpusIter.hasNext()) {
            String name = "GPU " + gpuCounter;
            GPUService gpu = new GPUService(name, gpusIter.next());
            bus.register(gpu);
            Thread thread = new Thread(gpu, gpu.getName() + " Thread");
            thread.start();
            gpuCounter++;
        }
        // Registering and executing each GPU from the gpus list as a new MicroService in the system
        int cpuCounter = 1;
        Iterator<CPU> cpusIter = cpus.iterator();
        while (cpusIter.hasNext()) {
            String cpuName = "CPU " + cpuCounter;
            CPUService cpu = new CPUService(cpusIter.next(), cpuName);
            bus.register(cpu);
            Thread thread = new Thread(cpu, cpu.getName() + " Thread");
            thread.start();
            cpuCounter++;
        }
        // Registering and executing each ConferenceInformation from the confInfos list as a new MicroService in the system
        Iterator<ConfrenceInformation> confsIter = confInfos.iterator();
        while (confsIter.hasNext()) {
            ConferenceService conf = new ConferenceService(confsIter.next());
            bus.register(conf);
            Thread thread = new Thread(conf, "Conference " + conf.getName() + " Thread");
            thread.start();
        }
        // Registering and executing each Student from the students list as a new MicroService in the system
        Iterator<Student> studentsIter = students.iterator();
        while (studentsIter.hasNext()) {
            StudentService student = new StudentService(studentsIter.next());
            bus.register(student);
            Thread thread = new Thread(student, "Student " + student.getName() + " Thread");
            thread.start();
        }
        // Starting the clock
        Thread timeThread = new Thread(timeService, "Time Thread");
        timeThread.start();

        // Waiting for the execution of the system to finish
        while (!timeService.isTimeOver()) {
            try {
                Thread.currentThread().sleep((duration*tickTime)/10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Generating output file
        File outputFile = new File("output.txt");
        try {
            FileWriter writer = new FileWriter(outputFile);
            writer.write("Students:\n");
            for (Student student : students) {
                writer.write(student.toString());
            }
            writer.write("Conferences:\n");
            for (ConfrenceInformation confInfo : confInfos) {
                writer.write(confInfo.toString());
            }
            writer.write(cluster.getStats());
            writer.flush();
            writer.close();
        }
        catch (IOException exc) {

        }
    }
}
