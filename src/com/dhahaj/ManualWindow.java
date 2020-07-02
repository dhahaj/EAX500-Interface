package com.dhahaj;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.*;
import org.firmata4j.IODevice;
import org.firmata4j.Pin;
import org.firmata4j.firmata.*;

import static processing.core.PApplet.println;

/**
 Window that displays a manual interface for control the IO_DIR pins.

 @author Daniel
 */
public final class ManualWindow extends JFrame implements WindowListener, ActionListener, DevicePins {

    java.util.List<JButton> buttons = new ArrayList<>();
    private final IODevice device;

    public ManualWindow(String title, IODevice device) {
        super(title);
        if (!device.isReady()) {
            JOptionPane.showMessageDialog(null, "Tester is not ready!");
            this.dispose();
        }
        this.device = device;
        setLayout(new GridLayout(3, 3));
        super.setSize(400, 500);
        init();
    }

    public void init() {
        int i = 0;
        for (int p : PINS) {
            Font f = new Font("Arial", Font.BOLD, 22);
            JButton b = new JButton("<html><center><b><font color=#ceffdd>" + NAMES[i] + "</b></font></html>");
            b.setSize(200, 170);
            b.setFont(f);
            b.setBackground(Color.GRAY);
            b.setActionCommand(String.valueOf(PINS[i++]));
            b.addActionListener(this);
            getContentPane().add(b);
            buttons.add(b);
        }

        addWindowListener(this);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(3, 2));
        setPreferredSize(new Dimension(500, 400));

        pack();
        setAlwaysOnTop(true);
        setVisible(true);
    }

    @Override
    public void windowOpened(WindowEvent e) {
    }

    @Override
    public void windowClosing(WindowEvent e) {
        try {
            setAllLow();
            device.getPin(INDICATOR_PIN).setValue(1);
        } catch (IOException | IllegalStateException ex) {
        }
    }

    @Override
    public void windowClosed(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }

    private void setAllLow() {
        int j = 0;
        for (int k : PINS) {
            setPin(device.getPin(PINS[j++]), 0);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JButton b = (JButton) e.getSource();
        String command = e.getActionCommand();
        int value = new Integer(command);
        if (b.getBackground() == Color.GRAY) {
            // GUIFrame.getArduino().dWrite(value, Arduino.HIGH);
            setPin(device.getPin(value), 1);
            b.setBackground(Color.DARK_GRAY);
            println("Pin=" + value + ", value=" + 1);
        } else {
            setPin(device.getPin(value), 0);
            b.setBackground(Color.GRAY);
            println("Pin=" + value + ", value=" + 0);
        }
    }

    private void setPin(Pin p, int value) {
        if (p.getMode() == Pin.Mode.OUTPUT) {
            try {
                p.setValue(value);
            } catch (IOException | IllegalStateException ex) {
                Logger.getLogger(ManualWindow.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

//    public static void main(String args[]) {
//        new ManualWindow("Test").setVisible(true);
//    }
}
