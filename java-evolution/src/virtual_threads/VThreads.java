package virtual_threads;

import java.io.*;
import java.nio.file.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/// Benchmark platform threads vs virtual threads
/// Never trust someone else's benchmarks - profile
/// your own application instead!
public class VThreads {
    final BufferedWriter out =
            Files.newBufferedWriter(Path.of("/tmp/id"));
    Runnable ioBoundRunnable = () -> {
        try {
            out.write("A");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    };
    public static int number = 0;
    Runnable cpuBoundRunnable = () -> {
        for (int i = 0; i < 100000; i++) {
            number++;
        }
    };

    public VThreads() throws IOException {
        // Default constructor to handle exceptions from opening "out"
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Hello and welcome to Virtual Threads!");
        VThreads demo = new VThreads();
        ExecutorService oldPool = Executors.newCachedThreadPool();
        demo.takeTime("Traditional Threads, IO busy", oldPool, demo.ioBoundRunnable);

        ExecutorService newPool = Executors.newVirtualThreadPerTaskExecutor();
        demo.takeTime("Virtual Threads, IO busy", newPool, demo.ioBoundRunnable);

        demo.takeTime("Traditional Threads, CPU bound", Executors.newCachedThreadPool(), demo.cpuBoundRunnable);

        demo.takeTime("Virtual Threads, CPU bound", Executors.newVirtualThreadPerTaskExecutor(), demo.cpuBoundRunnable);
    }

    public void takeTime(String descr, ExecutorService pool, Runnable r) throws IOException {
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            pool.submit(r);
        }
        pool.shutdown();
        pool.close();
        out.flush();
        long t1 = System.currentTimeMillis();
        System.out.println(descr + " elapsed time " + (t1-t0) + "mSec");
    }
}