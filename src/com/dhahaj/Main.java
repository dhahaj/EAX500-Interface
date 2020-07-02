/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dhahaj;

import com.dhahaj.UsersXML.User;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.text.*;
import java.util.*;
import java.util.logging.*;
import java.util.prefs.*;
import java.util.regex.*;
import javax.swing.*;
import javax.swing.text.*;
import jssc.*;
import org.firmata4j.*;
import org.firmata4j.firmata.*;
import processing.core.*;

import static com.dhahaj.Constants.AUTOTEST_PREF;
import static com.dhahaj.Constants.USER_XML_FILE;
import static jssc.SerialNativeInterface.getOsType;
import static org.firmata4j.Pin.Mode.*;
import static processing.core.PApplet.println;

/**
 Main class entry for the testing interface

 @author dmh
 */
public class Main implements Constants, DevicePins {

    private static final Logger LOGGER = LogControler.getLogger();
    private static final InterfaceWindow INTERFACE = new InterfaceWindow();
    public static final boolean DEBUG;
    private static final JFrame INITIALIZATION_FRAME = new JFrame();
    private static IODevice DEVICE;
    private static final UsersXML userXML = UsersXML.read(USER_XML_FILE);
    private static final Preferences prefs = Preferences.userRoot();
    private static StringBuffer buffer = new StringBuffer();
    private static PerformTest test = null;
    private static ActionListener ACTION_LISTENER;
    private static final SingleInstance SINGLE_INSTANCE;
    private static final IODeviceEventListener DEVICE_LISTENER;
    private static final javax.swing.Timer blinkTimer = new javax.swing.Timer(100, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (DEVICE != null && DEVICE.isReady()) {
                long value = DEVICE.getPin(INDICATOR_PIN).getValue();
                try {
                    DEVICE.getPin(INDICATOR_PIN).setValue(value == 0 ? 1 : 0);
                } catch (IOException | IllegalStateException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    });

    static {
        DEBUG = prefs.getBoolean("DEBUG", true);
        SINGLE_INSTANCE = new SingleInstance();
        DEVICE_LISTENER = new IODeviceEventListener() {
            @Override
            public void onStart(IOEvent event) {
                if (DEBUG) {
                    println(Thread.currentThread().getName());
                }
                FirmataDevice device = (FirmataDevice) event.getDevice();
                try {
                    device.sendMessage(FirmataMessageFactory.analogReport(false));
                } catch (IOException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }

                Pin p = device.getPin(INDICATOR_PIN);
                try {
                    p.setValue(1);
                } catch (IOException | IllegalStateException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
                SwingUtilities.invokeLater(() -> {
                    INTERFACE.connectMenuItem.setText("Disconnect Tester");
                    INTERFACE.loginButton.addAncestorListener(new RequestFocusListener());
                    if (INTERFACE.protocolVersion == null && device.isReady()) {
                        INTERFACE.protocolVersion = device.getProtocol();
                    }
                });
                hideInitializationWindow();
            }

            @Override
            public void onStop(IOEvent event) {
                if (DEBUG) {
                    println(Thread.currentThread().getName());
                }
            }

            @Override
            public void onPinChange(IOEvent event) {
            }

            @Override
            public void onMessageReceive(IOEvent event, String message) {
            }
        };
    }

    /**
     Window Listener
     */
    private static final WindowAdapter WINDOW_ADAPTER = new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent e) {
            LOGGER.entering(Main.class.getName(), "windowClosing");
            LOGGER.info("windowClosing");
            println(Thread.currentThread().getName());
            confirmExit();
            if (DEBUG) {
                println(e);
            }
        }

        @Override
        public void windowDeiconified(WindowEvent e) {
            println(e);
        }

        @Override
        public void windowIconified(WindowEvent e) {
            println(e);
        }

        @Override
        public void windowOpened(WindowEvent e) {
            println(e);
        }

        @Override
        public void windowStateChanged(WindowEvent e) {
            println(e);
        }

    };

    /**
     Handler for performing a login.
     */
    private static final ActionListener LOGIN_LISTENER = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            JComboBox cb = (JComboBox) e.getSource();
            String username = (String) cb.getSelectedItem();
            String pass = e.getActionCommand();
            // println(Thread.currentThread().getName());
            INTERFACE.passwordField.setText("");
            boolean loginOK = UsersXML.checkPassword(username, pass);
            if (loginOK) {
                if (UsersXML.findUser(username).isAdmin()) {
                    INTERFACE.adminMenu.setEnabled(true);
                }
                INTERFACE.LOGOUT_TIMER.start();
                INTERFACE.userLabel.setText(username);
                INTERFACE.userLabel.setForeground(GREEN);
                INTERFACE.loginButton.setText("Logout");
                INTERFACE.passwordDialog.setVisible(false);
                INTERFACE.pgmButton.setEnabled(true);
                INTERFACE.testButton.setEnabled(true);
                INTERFACE.controlPane.requestFocus();
                INTERFACE.selectButton.addAncestorListener(new RequestFocusListener());
                LogControler.USER = UsersXML.findUser(username);
                LOGGER.log(Level.INFO, "User Login Event: {0}", username);
            } else {
                INTERFACE.userComboBox.setEnabled(true);
                INTERFACE.adminMenu.setEnabled(false);
                INTERFACE.LOGOUT_TIMER.stop();
                INTERFACE.pgmButton.setEnabled(false);
                INTERFACE.testButton.setEnabled(false);
                INTERFACE.passwordDialog.setVisible(false);
                // JOptionPane.showMessageDialog(INTERFACE, "Incorrect Password!", "Error", JOptionPane.ERROR_MESSAGE);
                ErrorDialog.showMessageDialog(INTERFACE, "Incorrect Password!", "Error");
            }
        }
    };

    private static void populateUserComboBox() {
        JComboBox<String> box = INTERFACE.userComboBox;
        String name = (String) box.getSelectedItem();
        INTERFACE.userComboBox.removeAllItems();
        for (String s : userXML.getUserNames()) {
            box.addItem(s);
        }
        try {
            box.setSelectedItem(name);
        } catch (Exception e) {
        }
    }

    /**
     Show a dialog window to select the serial port.

     @return The selected port.
     */
    private static String requestPort() throws SerialPortException {
        Font f = new Font("Tahoma", Font.PLAIN, 20);
        JComboBox<String> portNameSelector = new JComboBox<>();
        portNameSelector.setFont(f);
        portNameSelector.setModel(new DefaultComboBoxModel<>());
        portNameSelector.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                println(e);
            }
        });
        String[] portNames;
        int osType = getOsType();
        switch (osType) {
        case SerialNativeInterface.OS_MAC_OS_X:
            portNames = SerialPortList.getPortNames("/dev/", Pattern.compile("tty\\..*"));
            break;
        case SerialNativeInterface.OS_WINDOWS:
            portNames = SerialPortList.getPortNames();
            break;
        default:
            throw new SerialPortException(null, "requestPort", SerialPortException.TYPE_PORT_NOT_FOUND);
        }

        println(Arrays.toString(portNames));
        java.util.List<String> names = new ArrayList<>();
        for (String portName : portNames) {
            portNameSelector.addItem(portName);
            names.add(portName);
        }

        if (names.contains(prefs.get(COM_PORT_PREF_KEY, null))) {
            println("Found saved COM port");
            return prefs.get(COM_PORT_PREF_KEY, null);
        }

        if (portNameSelector.getItemCount() == 0) {
            //JOptionPane.showMessageDialog(null, "Cannot find any serial port", "Error", JOptionPane.ERROR_MESSAGE);
            ErrorDialog.showMessageDialog(INTERFACE, "Cannot find any serial port", "Error");
            LOGGER.warning("No Serial Ports Available");
            System.exit(0);
            // return null;
        }

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setFont(f);
        Component[] components = panel.getComponents();

        for (Component c : components) {
            if (DEBUG) {
                println(c);
            }
            c.setFont(f);
        }

        JLabel label = new JLabel("Port ");
        label.setFont(f);
        panel.add(label);
        panel.add(portNameSelector);

        if (JOptionPane.showConfirmDialog(null, panel, "Select the port",
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            String s = portNameSelector.getSelectedItem().toString();
            prefs.put(COM_PORT_KEY, s);
            return portNameSelector.getSelectedItem().toString();
        } else {
            System.exit(0);
        }
        return "";
    }

    /**
     Display the initialization dialog.
     */
    private static void showInitializationMessage() {
        try {
            SwingUtilities.invokeAndWait(() -> {
                Font f = new Font("Tahoma", Font.BOLD, 22);
                JFrame frame = INITIALIZATION_FRAME;
                frame.setAlwaysOnTop(true);
                frame.setUndecorated(true);
                frame.setBackground(Color.BLUE);
                JLabel label = new JLabel("Connecting to tester");
                label.setFont(f);
                label.setHorizontalAlignment(JLabel.CENTER);
                frame.add(label);
                frame.pack();
                frame.setSize(frame.getWidth() + 80, frame.getHeight() + 80);
                frame.setLocationRelativeTo(INTERFACE);
                frame.setVisible(true);
            });
        } catch (InterruptedException | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     Hide the initialization dialog.
     */
    private static void hideInitializationWindow() {
        try {
            Thread.currentThread().sleep(1000);
            SwingUtilities.invokeAndWait(() -> {
                INITIALIZATION_FRAME.setVisible(false);
                INITIALIZATION_FRAME.dispose();
            });
        } catch (InterruptedException | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     Helper method for confirming a window closing event.
     */
    public static void confirmExit() {
        int option = JOptionPane.showConfirmDialog(INTERFACE, "Close Software?", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (option == JOptionPane.YES_OPTION) {
            if (DEVICE != null) {
                for (Pin p : DEVICE.getPins()) {
                    if (p.getMode() == Pin.Mode.OUTPUT) {
                        try {
                            p.setValue(0);
                        } catch (IOException | IllegalStateException ex) {
                            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                try {
                    DEVICE.stop();
                } catch (IOException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            LOGGER.info("Software Closing.");
            exportPrefs(BASE_DIR + "prefs.xml", prefs);
            INTERFACE.dispose();
            System.exit(0);
        }
    }

    /**
     Export provided Preferences to an XML document.

     @param path  Path to the file.
     @param prefs Preferences to save.
     */
    public static void exportPrefs(String path, Preferences prefs) {
        println("divider location = " + prefs.get(DIVIDER_LOCATION_PREF, null));
        try {
            prefs.flush();
            prefs.sync();
        } catch (BackingStoreException e1) {
        }
        OutputStream outStream = null;
        try {
            outStream = new FileOutputStream(path);
            prefs.exportNode(outStream);
        } catch (IOException | BackingStoreException e) {
        } finally {
            try {
                if (outStream != null) {
                    outStream.close();
                }
            } catch (IOException e) {
            }
        }
    }

    public static void appendText(String s) {
        buffer.append(s).append('\n');
        SwingUtilities.invokeLater(() -> {
            INTERFACE.textArea.setText(buffer.toString());
        });
    }

    private static void setupListeners() {

        // Remove user dialog window
        INTERFACE.miRemoveUser.addActionListener((ActionEvent e) -> {
            INTERFACE.jComboBox1.removeAllItems();
            String[] users = userXML.getUserNames().toArray(new String[userXML.getList().size()]);
            for (String s : users) {
                INTERFACE.jComboBox1.addItem(s);
            }
            INTERFACE.jComboBox1.addAncestorListener(new RequestFocusListener());
            INTERFACE.removeUserDialog.setVisible(true);
        });

        INTERFACE.removeUserOKBtn.addActionListener((ActionListener) -> {
            String selectedUser = (String) INTERFACE.jComboBox1.getSelectedItem();
            userXML.removeUser(selectedUser);
            setUsers(userXML);
            UsersXML.saveXML(USER_XML_FILE, userXML);
            populateUserComboBox();
            INTERFACE.removeUserDialog.setVisible(false);
        });

        // Add user dialog listener
        INTERFACE.newUserDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                INTERFACE.userTextField.setText("");
                INTERFACE.passwordField1.setText("");
                INTERFACE.adminCheckBox.setSelected(false);
            }

        });

        // Clear Screen Button
        INTERFACE.btnClearScreen1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buffer = new StringBuffer();
            }
        });

        // Connect Tester Listener
        INTERFACE.connectMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (DEVICE == null) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {

                            try {
                                INTERFACE.connectMenuItem.setText("Disconnect Tester");
                                String port = requestPort();
                                DEVICE = new FirmataDevice(port);
                                DEVICE.start();
                                DEVICE.ensureInitializationIsDone();
                                DEVICE.addEventListener(DEVICE_LISTENER);
                                DEVICE.getPin(BATT_PIN).setMode(OUTPUT);
                                DEVICE.getPin(LOW_BATT_PIN).setMode(OUTPUT);
                                DEVICE.getPin(INDICATOR_PIN).setMode(OUTPUT);
                                DEVICE.getPin(BATT_PIN).setValue(0);
                                DEVICE.getPin(LOW_BATT_PIN).setValue(0);
                                DEVICE.getPin(INDICATOR_PIN).setValue(1);
                                INTERFACE.comErrorLabel.setVisible(false);
                            } catch (SerialPortException | IOException | InterruptedException ex) {
                                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                                DEVICE = null;
                                INTERFACE.comErrorLabel.setVisible(true);
                                INTERFACE.connectMenuItem.setText("Connect Tester");
                                if (ex instanceof IOException) {
                                    ErrorDialog.showMessageDialog(INTERFACE, ex.getMessage(), "Error");
                                } else {
                                    // JOptionPane.showMessageDialog(INTERFACE, "Error connecting to tester.");
                                    ErrorDialog.showMessageDialog(INTERFACE, "Error connecting to tester.", "Error");
                                }
                            }
                        }
                    });
                } else {
                    try {
                        INTERFACE.comErrorLabel.setVisible(true);
                        INTERFACE.connectMenuItem.setText("Connect Tester");
                        DEVICE.stop();
                        DEVICE = null;
                    } catch (IOException ex) {
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });

        // Ok button on new user dialog pressed
        INTERFACE.okBtn.addActionListener((ActionEvent e) -> {
            String u = INTERFACE.userTextField.getText();
            String p = new String(INTERFACE.passwordField1.getPassword());
            boolean a = INTERFACE.adminCheckBox.isSelected();
            INTERFACE.userTextField.setText("");
            INTERFACE.passwordField1.setText("");
            INTERFACE.adminCheckBox.setSelected(false);
            if (!u.isEmpty() && !p.isEmpty()) {
                User user = new User(u, p, a);
                println(user);
                userXML.addUser(user);
                userXML.setUserList(sortList(userXML));
                println(userXML.getUserNames());
                println(user);
                try {
                    UsersXML.saveXML(USER_XML_FILE, userXML);
                } catch (Exception ex) {
                    Logger.getLogger(InterfaceWindow.class.getName()).log(Level.SEVERE, null, ex);
                }
                populateUserComboBox();
            }
            INTERFACE.newUserDialog.setVisible(false);
        });

        // Show the new user dialog
        INTERFACE.miAddUser.addActionListener((ActionEvent e) -> {
            INTERFACE.newUserDialog.setVisible(true);
            INTERFACE.userTextField.addAncestorListener(new RequestFocusListener());
        });

        // EAX300 Menu Item
        INTERFACE.eax300menuItem.addActionListener((ActionEvent e) -> {
            int i = JOptionPane.showConfirmDialog(INTERFACE, "Program EAX300 Boards?", "Confirm", JOptionPane.OK_CANCEL_OPTION);
            if (i == JOptionPane.OK_OPTION) {
                INTERFACE.deviceLabel.setText("EAX300");
                INTERFACE.deviceLabel.setForeground(GREEN);
            }
        });

        // Login ComboBox
        INTERFACE.userComboBox.addItemListener((ItemEvent e) -> {
            println(e.getSource());
            JComboBox cb = (JComboBox) e.getSource();
            int i = cb.getSelectedIndex();
            prefs.putInt(USER_PREF_KEY, i);
        });

        // Manual Control Menu item
        INTERFACE.miManualControl.addActionListener((ActionEvent e) -> {
            if (DEVICE != null) {
                ManualWindow mw = new ManualWindow("Manual Control", DEVICE);
            } else {
                ErrorDialog.showMessageDialog(INTERFACE, "Tester not connected!", "Error");
            }
        });

        // Program Button
        INTERFACE.pgmButton.addActionListener((ActionEvent e) -> {
            String deviceSelection = INTERFACE.deviceLabel.getText();
            doProgramming();
        });
    }

    private static void showInstructions() {
        buffer = new StringBuffer();
        String res = RESOURCE_DIRECTORY + "instructions.txt";
        println(res);
        if (res != null) {
            String inst = readFile(INSTRUCTIONS_FILE);
            appendText(inst);
        }
    }

    /**
     Reads text from a <b>File</b> object.

     @param file A <b>File</b> object to read.
     @return A string of the <b>File's</b> contents.
     */
    @SuppressWarnings("ConvertToTryWithResources")
    static String readFile(java.io.File file) {
        if (file == null || file.isDirectory() || !file.exists()) {
            return null;
        }

        java.util.List<String> lines;
        try {
            lines = new ArrayList<>(Files.readAllLines(file.toPath()));
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }

        StringBuilder contents = new StringBuilder();
        lines.forEach((line) -> {
            contents.append(line).append(NEWLINE);
        });
        return contents.toString();
    }

    private static void doLogout() {
        String oldUser = INTERFACE.userLabel.getText();
        INTERFACE.LOGOUT_TIMER.stop();
        INTERFACE.pgmButton.setEnabled(false);
        INTERFACE.testButton.setEnabled(false);
        INTERFACE.passwordDialog.setVisible(false);
        INTERFACE.deviceLabel.setText("No Device Selected");
        INTERFACE.deviceLabel.setForeground(Color.RED);
        INTERFACE.loginButton.setText("Login");
        INTERFACE.userLabel.setText("Not Logged In");
        INTERFACE.userLabel.setForeground(Color.RED);
        INTERFACE.adminMenu.setEnabled(false);
        INTERFACE.userComboBox.setEnabled(true);
        if (test != null) {
            test.stop();
        }
        LOGGER.info(String.format("%s user logged off.", oldUser));
    }

    private static void setUsers(UsersXML xml) {
        java.util.List<User> userList = xml.getList();
        String name = (String) INTERFACE.userComboBox.getSelectedItem();
        println(name);
        int index = INTERFACE.userComboBox.getSelectedIndex();
        INTERFACE.userComboBox.removeAllItems();
        for (User u : userList) {
            String n = u.getName();
            INTERFACE.userComboBox.addItem(n);
        }
        INTERFACE.userComboBox.setSelectedItem(name);
        //        if (index - 1 < INTERFACE.userComboBox.getItemCount()) {
        //            INTERFACE.userComboBox.setSelectedIndex(index - 1);
        //        } else {
        //            INTERFACE.userComboBox.setSelectedIndex(0);
        //        }
    }

    private static void doProgramming() {
        String firmware = INTERFACE.deviceLabel.getText();
        if (firmware.contains("No")) {
            // JOptionPane.showMessageDialog(INTERFACE, "No device selected.", "Error", JOptionPane.ERROR_MESSAGE);
            ErrorDialog.showMessageDialog(INTERFACE, "No device selected.", "Error");
            return;
        }
        blinkTimer.start();
        Calendar calendar = GregorianCalendar.getInstance();
        Date d = calendar.getTime();

        StringBuilder builder = new StringBuilder("label: EE.20 20 ");

        // Add the device name
        appendToSerialText(builder, firmware);

        // Add the username
        appendToSerialText(builder, INTERFACE.userLabel.getText());

        Calendar cal = new GregorianCalendar();
        // Get the date
        int Y = cal.get(Calendar.YEAR), M = cal.get(Calendar.MONTH) + 1, D = cal.get(Calendar.DATE);

        // Get the time
        String h = Integer.toString(cal.get(Calendar.HOUR));
        if (h.equals("0")) {
            h = "12";
        }

        String m = Integer.toString(cal.get(Calendar.MINUTE));
        if (m.length() < 2) {
            m = "0" + m;
        }

        // Write the date
        appendToSerialText(builder, String.format("%d/%d", M, D));

        // Write the time
        String AM_PM;
        if (cal.get(Calendar.HOUR_OF_DAY) > 11) {
            AM_PM = "PM";
        } else {
            AM_PM = "AM";
        }
        String time = String.format("%s.%s%s", h, m, AM_PM);
        appendToSerialText(builder, time);

        builder.append(" ;\r\n");
        println("Programming argument = " + builder.toString());

        try {
            // Files.deleteIfExists(Paths.get(SERIAL_PATH));
            File f = new File(SERIAL_PATH);
            //f.createNewFile();
            FileWriter fw = new FileWriter(f);
            //fw.write('\n');
            fw.write(builder.toString());
            fw.write("\r\n");
            fw.flush();
            fw.close();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

        INTERFACE.pgmButton.setEnabled(false);
        INTERFACE.testButton.setEnabled(false);

        SwingWorker sw = new SwingWorker() {

            int i = -1;

            @Override
            protected Object doInBackground() throws Exception {
                println(Thread.currentThread().getName());
                try {
                    String arg;
                    if (firmware.contains("EAX300")) {
                        arg = String.format("up.exe %s /p \"%s%s%s\"",
                                PROGRAMMING_ARGS,
                                FIRMWARE_DIRECTORY,
                                firmware,
                                "_5.9.hex");
                    } else {
                        arg = String.format("up.exe %s /p \"%s%s%s\"",
                                PROGRAMMING_ARGS,
                                FIRMWARE_DIRECTORY,
                                firmware,
                                "_591.hex");
                    }
                    println(arg);
                    Process p = PApplet.launch(arg);
                    p.waitFor();
                    i = p.exitValue();
                } catch (InterruptedException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
                return i;
            }

            @Override
            protected void done() {
                super.done();
                INTERFACE.pgmButton.setEnabled(true);
                INTERFACE.testButton.setEnabled(true);
                // appendText("Programming Returned: " + String.valueOf(i));
                LOGGER.info(String.format("Programming Device:%s, Return Value=%d", INTERFACE.deviceLabel.getText(), i));
                blinkTimer.stop();
                if (DEVICE != null) {
                    try {
                        DEVICE.getPin(INDICATOR_PIN).setValue(1);
                    } catch (IOException | IllegalStateException ex) {
                    }
                }
                if (i != 0) {
                    ErrorDialog.showMessageDialog(INTERFACE, "Programming Error", "Error");
                    return;
                }
                if (prefs.getBoolean(AUTOTEST_PREF, true)) {
                    INTERFACE.testButton.doClick();
                }
            }
        };
        sw.execute();
    }

    private static void appendToSerialText(StringBuilder builder, String text) {
        char[] chars = text.toCharArray();
        for (char c : chars) {
            builder.append("'").append(c).append("'").append(" ");
        }
        builder.append(SERIAL_SPACE_STRING).append(" ");
    }

    public static void main(String args[]) throws IOException, InterruptedException,
            SerialPortException, InvalidPreferencesFormatException, BackingStoreException {

        // Check if a lock is present
        if (SINGLE_INSTANCE.isLocked()) {
            // FJOptionPane.showMessageDialog(null, "Software Already Running", "Error", JOptionPane.ERROR_MESSAGE);
            ErrorDialog.showMessageDialog(null, "Software Already Running!", "Error");
            System.exit(0);
        }

        // Import the saved preference file.
        Preferences.importPreferences(new FileInputStream(PREFERENCE_FILE));
        // prefs = Preferences.userRoot();
        INTERFACE.setPrefs(prefs);

        LogControler.FilePath = BASE_DIR + "log";
        LogControler.setup();
        LOGGER.info("Software starting.");
        AbstractAction addTextListener = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buffer.append(e.getActionCommand()).append("\n");
                INTERFACE.textArea.setText(buffer.toString());
            }
        };

        ACTION_LISTENER = (ActionEvent e) -> {
            String cmd = e.getActionCommand();
            if (cmd.contains("Set COM")) {
                if (DEBUG) {
                    println(cmd);
                }
                // TODO: add changing com port here
                try {
                    String port = requestPort();
                } catch (SerialPortException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (cmd.contains("Test")) { // Start testing
                if (DEVICE == null) {
                    // JOptionPane.showMessageDialog(INTERFACE, "Tester not connected!", "Error", JOptionPane.ERROR_MESSAGE);
                    ErrorDialog.showMessageDialog(INTERFACE, "Error connecting to tester.", "Error");
                    return;
                } else if (!DEVICE.isReady()) {
                    // JOptionPane.showMessageDialog(INTERFACE, "Tester not ready!", "Error", JOptionPane.ERROR_MESSAGE);
                    ErrorDialog.showMessageDialog(INTERFACE, "Tester not ready!", "Error");
                    return;
                }

                LOGGER.info("Testing started.");
                int speed = prefs.getInt(TEST_SPEED_PREF_KEY, 1000);
                double scaler = prefs.getDouble(SLIDER_PREF_KEY, 1);
                scaler = (scaler / 100.0);
                println("Scaler=" + scaler);
                speed += scaler * speed;
                println("Speed=" + speed);
                test = new PerformTest(INTERFACE, DEVICE, speed);
                // INTERFACE.LOGOUT_TIMER.restart();
                INTERFACE.LOGOUT_TIMER.stop();
                buffer = new StringBuffer();
                INTERFACE.textArea.setText("");
                INTERFACE.testButton.setEnabled(false);
                INTERFACE.pgmButton.setEnabled(false);
                test.setAction(addTextListener);
                test.setListener(ACTION_LISTENER);
                test.start();
            } else if (cmd.contains("Continue")) { // Finish testing
                test.stop();
            } else if (cmd.contains("Finished")) { // End Testing
                LOGGER.info("Testing finished.");
                test = null;
                INTERFACE.testButton.setEnabled(true);
                INTERFACE.testButton.setText("Test");
                INTERFACE.pgmButton.setEnabled(true);
                showInstructions();
            }
        };

        String p = requestPort();
        if (p != null) {
            DEVICE = new FirmataDevice(p);
        } else {
            DEVICE = null;
        }

        showInitializationMessage();

        DEVICE.addEventListener(DEVICE_LISTENER);

        SwingUtilities.invokeLater(Main::intializeWindow);

        try {
            DEVICE.start();
            DEVICE.ensureInitializationIsDone();
            DEVICE.getPin(BATT_PIN).setMode(OUTPUT);
            DEVICE.getPin(LOW_BATT_PIN).setMode(OUTPUT);
            DEVICE.getPin(INDICATOR_PIN).setMode(OUTPUT);
            DEVICE.getPin(BATT_PIN).setValue(0);
            DEVICE.getPin(LOW_BATT_PIN).setValue(0);
            DEVICE.getPin(INDICATOR_PIN).setValue(1);
        } catch (IOException | InterruptedException e) {
            DEVICE = null;
            hideInitializationWindow();
            ErrorDialog.showMessageDialog(INTERFACE, e.getMessage(), "Error");
            // JOptionPane.showMessageDialog(INTERFACE, "Error connecting to tester.");
            // ErrorDialog.showMessageDialog(INTERFACE, "Error connecting to tester.", "Error");
            // JOptionPane.showMessageDialog(INTERFACE, "Error connecting to tester.");
            // ErrorDialog.showMessageDialog(INTERFACE, "Error connecting to tester.", "Error");
            // LOGGER.warning("Error connecting to tester.");
            LOGGER.warning(e.getMessage());
            INTERFACE.comErrorLabel.setVisible(true);
            //INTERFACE.dispose();
            // System.exit(0);
        }
    }

    private static void intializeWindow() {
        java.util.List list = sortList(userXML);
        userXML.setUserList(list);

        INTERFACE.setExtendedState(JFrame.MAXIMIZED_BOTH);

        // Populate the login combo box and select the last known user
        populateUserComboBox();
        try {
            INTERFACE.userComboBox.setSelectedIndex(prefs.getInt(USER_PREF_KEY, 0));
        } catch (Exception e) {
        }

        // Set the autotest menu item
        INTERFACE.miAutotest.setSelected(prefs.getBoolean(AUTOTEST_PREF, true));

        // Initiate the autologout timer.
        INTERFACE.LOGOUT_TIMER.addActionListener((ActionEvent e) -> {
            LOGGER.info("User auto logoff.");
            doLogout();
        });
        INTERFACE.LOGOUT_TIMER.setInitialDelay(prefs.getInt(AUTOLOGOUT_PREF, 60000 * 3));
        INTERFACE.newUserDialog.setLocationRelativeTo(null);

        // Set the saved font.
        String name = prefs.get(FONT_NAME_PREF, "Cambria");
        int style = prefs.getInt(FONT_STYLE_PREF, 1);
        int size = prefs.getInt(FONT_SIZE_PREF, 18);
        Font f = new Font(name, style, size);
        INTERFACE.textArea.setFont(f);

        INTERFACE.jDeviceList.setSelectedIndex(0);
        INTERFACE.textArea.setBackground(new Color(prefs.getInt(BKGD_PREF_KEY, 0)));
        INTERFACE.textArea.setForeground(new Color(prefs.getInt(FRGD_PREF_KEY, 0)));
        INTERFACE.addPasswordDialogListener(LOGIN_LISTENER);
        INTERFACE.jSplitPane2.setDividerLocation(prefs.getDouble(DIVIDER_LOCATION_PREF, 500));
        //println("divider location = " + INTERFACE.jSplitPane2.getDividerLocation());
        INTERFACE.slider.setValue(prefs.getInt(SLIDER_PREF_KEY, 50));
        INTERFACE.testButton.addActionListener(ACTION_LISTENER);
        INTERFACE.pgmButton.setEnabled(false);
        INTERFACE.testButton.setEnabled(false);
        INTERFACE.miSetCOMPort.addActionListener(ACTION_LISTENER);
        INTERFACE.addWindowListener(WINDOW_ADAPTER);
        showInstructions();
        setupListeners();
        INTERFACE.setVisible(true);
    }

    private static java.util.List sortList(UsersXML xml) {
        java.util.List list = xml.getList();
        Comparator c = (Comparator) (Object o1, Object o2) -> {
            User u1 = (User) o1;
            User u2 = (User) o2;
            String s1 = u1.getName(), s2 = u2.getName();
            s1 = s1.toUpperCase();
            s2 = s2.toUpperCase();
            char c1 = s1.charAt(0);
            char c2 = s2.charAt(0);
            if (c1 == c2) {
                if (u1 != u2) {
                    c1 = s1.charAt(1);
                    c2 = s2.charAt(1);
                    return c1 == c2 ? 0 : -1;
                }
                return 0;
            } else if (c1 < c2) {
                return -1;
            } else if (c1 > c2) {
                return 1;
            }
            return 0;
        };
        list.sort(c);
        return list;
    }
}
