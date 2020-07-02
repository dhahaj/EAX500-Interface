package com.dhahaj;

//import static com.tester.DebugLogger.debug;
//import static com.tester.Constants.BASE_DIR;
import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.logging.Level;
import java.util.logging.Logger;
import static processing.core.PApplet.println;

/**
 Class which allows only a single instance to run

 @author Daniel
 */
public class SingleInstance implements Constants {

    private File f;
    private FileChannel channel;
    private FileLock lock;
    private boolean locked;
//    private final InterfaceWindow parent;

    /**
     Main Constructor

     @param parent The processing applet.
     */
    public SingleInstance() {
//        this.parent = parent;
        String name = Thread.currentThread().getName();
        println("SingleInstanceConstructor, thread name = " + name);
        start();
    }

    /**
     Starts the single instance.
     */
    public void start() {
        locked = false;
        try {
            f = new File(LOCK_FILE);

            // Check if the lock exist
            if (f.exists()) {
                f.delete(); // if exist try to delete it
            }

            // Try to get the lock
            channel = new RandomAccessFile(f, "rw").getChannel();
            lock = channel.tryLock();

            if (lock == null) {
                // File is locked by other application
                channel.close();
                locked = true;
                //throw new RuntimeException("Application is locked!");
            }

            // Add shutdown hook to release lock when application shutdown
            ShutdownHook shutdownHook = new ShutdownHook();
            EventQueue.invokeLater(() -> {
                Runtime.getRuntime().addShutdownHook(shutdownHook);
            });
            // Runtime.getRuntime().addShutdownHook(shutdownHook);

            // Your application tasks here..
            System.out.println("Running");
        } catch (IOException e) {
            println(e);
            println(Thread.currentThread().getName());
            throw new RuntimeException("Could not start process.", e);
        } catch (OverlappingFileLockException oe) {
            println("Thread name: " + Thread.currentThread().getName());
        }
    }

    public boolean isLocked() {
        return locked;
    }

    /**
     Release and delete file lock.

     @return
     */
    public boolean unlockFile() {
        boolean b = false;
        try {
            if (lock != null) {
                lock.release();
                channel.close();
                b = f.delete();
                System.out.println("File Unlocked: " + b);
                if (!b) {
                    println("Unable to unlock file.");
                }
            }
        } catch (IOException e) {
        }
        return b;
    }

    class ShutdownHook extends Thread {

        @Override
        public synchronized void start() {
            super.start(); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void run() {
            //            try {
            //                Files.createTempFile(BASE_DIR + "filelock", ".lock");
            //            } catch (IOException ex) {
            //                Logger.getLogger(SingleInstance.class.getName()).log(Level.SEVERE, null, ex);
            //            }
            println("shutting down...");
            boolean b = unlockFile();
            if (!b) {
                println("Unable to unlock file!");
            }
        }

    }

}
