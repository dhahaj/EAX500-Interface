package com.dhahaj;

import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.logging.*;
import javax.swing.*;
import javax.swing.Timer;
import org.firmata4j.*;

import static org.firmata4j.Pin.Mode.OUTPUT;
import static processing.core.PApplet.println;

/**
 Performs the testing procedure.

 @author dmh
 */
public class PerformTest implements ActionListener, DevicePins {

    private final InterfaceWindow parent;
    private final IODevice device;
    private final Timer timer;
    private final AtomicBoolean finished = new AtomicBoolean(false);
    private final AtomicBoolean waiting = new AtomicBoolean(false);
    private int state = 0;
    private Pin battery;
    private Pin lowBattery;
    private Pin indicatorPin;
    private Pin testIndPin;
    private AbstractAction action;
    private ActionListener listener;
    private static final HashMap<Integer, String> PIN_MAP = new HashMap<Integer, String>();
    private long timestamp;
    final Object lock = new Object();

    static {
        int k = 0;
        for (int i : PINS) {
            PIN_MAP.put(i, NAMES[k++]);
        }
        println(PIN_MAP.keySet());
        println(PIN_MAP.values());
    }

    public PerformTest(InterfaceWindow parent, IODevice device, int speed) {
        this.parent = parent;
        this.device = device;
        timer = new Timer(speed, this);
        if (!device.isReady()) {
            JOptionPane.showMessageDialog(parent, "Tester not ready!", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            battery = this.device.getPin(BATT_PIN);
            lowBattery = this.device.getPin(LOW_BATT_PIN);
            indicatorPin = this.device.getPin(INDICATOR_PIN);
            testIndPin = this.device.getPin(TESTING_INDICATOR_PIN);
            initPins();
        }
    }

    /**
     Set the ActionListener.

     @param listener The {@link ActionListener}.
     */
    public void setListener(ActionListener listener) {
        this.listener = listener;
    }

    /**
     Set the ActionListener.

     @param a The {@link AbstractAction}.
     */
    public void setAction(AbstractAction a) {
        action = a;
    }

    /**
     Starts the test.
     */
    public void start() {
        timestamp = System.currentTimeMillis();
        parent.LOGOUT_TIMER.stop();
        timer.setInitialDelay(0);
        timer.start();
        setPin(indicatorPin, 1);
        setPin(testIndPin, 1);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        synchronized (lock) {
           
            if (finished.get() == false) {
                switch (state) {

                case 0:
                    setPin(battery, 1);
                    setText("**** Starting EAX-500 Test ****");
                    setText("Battery ON");
                    state++;
                    break;
                case 2:
                    setPin(lowBattery, 1);
                    setText(" → Starting Low Battery Test - The Red & Green LED's blink alternately");
                    break;
                case 4:
                    setPin(lowBattery, 0);
                    setText(" → Low Battery OFF");
                    state += 2;

                case 6:
                    setText("\nPerform the following:\n"
                            + "  → Place a magnet on the right reed switch - The Green LED lights up\n"
                            + "  → Place a magnet on the left reed switch - The Red LED lights up\n"
                            + "  → Press the right switch - The Red & Green LED's flash\n"
                            + "  → Press the left switch - The Siren sounds\n\n"
                            + "** Press the continue button below to end the test **");
                    waiting.set(true);
                    timer.setDelay(25);
                    parent.testButton.setEnabled(true);
                    parent.testButton.setText("Continue");
                    parent.testButton.requestFocusInWindow();
                    state++;
                case 7:
                    if (waiting.get()) {
                        if (System.currentTimeMillis() - timestamp > 60000) {
                            endTest();
                        }
                        return;
                    } else {
                        finished.set(true);
                    }
                    break;
                }

                if (finished.get()) {
                    //listener.actionPerformed(new ActionEvent(this, 0, "Finished"));
                    //println("Finished");
                    endTest();
                } else {
                    state++;
                }
            }
        }
    }

    // Initialize all PINS as outputs and set them low. 
    private void initPins() {
        for (int i : PINS) {
            final Pin p = device.getPin(i);
            try {
                p.setMode(OUTPUT);
                p.setValue(0);
            } catch (IOException | IllegalArgumentException ex) {
                Logger.getLogger(PerformTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void setPin(int p, int v) {
        try {
            device.getPin(p).setValue(v);
        } catch (IOException | IllegalStateException ex) {
            Logger.getLogger(PerformTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void setPin(Pin p, int v) {
        try {
            p.setValue(v);
        } catch (IOException | IllegalStateException ex) {
            Logger.getLogger(PerformTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void setText(String s) {
        action.actionPerformed(new ActionEvent(this, 0, s));
    }

    private void setAllLow() {
        for (int i : PINS) {
            setPin(device.getPin(i), 0);
        }
        setPin(device.getPin(INDICATOR_PIN), 1);
    }

    private void endTest() {
        timer.stop();
        setAllLow();
        parent.LOGOUT_TIMER.restart();
        // parent.testButton.doClick();
        listener.actionPerformed(new ActionEvent(this, 0, "Finished"));
    }

    /**
     Stops the test.
     */
    public void stop() {
        endTest();
    }

}
