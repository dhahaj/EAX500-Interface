package com.dhahaj;

import java.awt.*;
import java.io.File;
import org.apache.commons.lang3.SystemUtils;

/**
 Defines constants for use throughout the application.

 @author dmh
 */
public interface Constants {

    /**
     Base directory for the running application.
     */
    public final static String BASE_DIR = SystemUtils.getUserDir().getAbsolutePath() + File.separatorChar; //(new File("").getAbsolutePath()) + File.separatorChar; //"C;\\TesterInterface";

    /**
     Path to the Resource folder.
     */
    public final static String RESOURCE_DIRECTORY = BASE_DIR + "Resources" + File.separatorChar;

    public final static String VIDEO_DIRECTORY = RESOURCE_DIRECTORY + "Videos";

    /**
     Path to the folder containing the firmware files.
     */
    public final static String FIRMWARE_DIRECTORY = RESOURCE_DIRECTORY + "firmware" + File.separatorChar;

    /**
     The lock file to run a single instance of this program.
     */
    public final static String LOCK_FILE = BASE_DIR + "singleinstance.lock";

    /**
     The key value of the COM port preference value.
     */
    public static final String COM_PORT_PREF_KEY = "COM_PORT";

    public static final String USER_PREF_KEY = "USER_INDEX";
    public static final String BKGD_PREF_KEY = "BKGD_CLR";
    public static final String FRGD_PREF_KEY = "FRGD_CLR";
    public static final String COMPILE_DATE = "July 2, 2020 11:37AM";
    public static final String REVISION = "Revision 2.0.4";

    public static final String SLIDER_PREF_KEY = "SLIDER_VALUE";
    public static final String TEST_SPEED_PREF_KEY = "TESTING_SPEED";
    public static final String FONT_NAME_PREF = "FontName";
    public static final String FONT_STYLE_PREF = "FontStyle";
    public static final String FONT_SIZE_PREF = "FontSize";
    public static final String DIVIDER_LOCATION_PREF = "DIV_LOCATION";
    public static final String AUTOTEST_PREF = "AUTOTEST";
    public static final String AUTOLOGOUT_PREF = "AUTOLOGOUT";
    public static final String FRAME_WIDTH = "FRAME_WIDTH";
    public static final String FRAME_HEIGHT = "FRAME_HEIGHT";

    /**
     Path to the XML file for storing the user data.
     */
    public static final String USER_XML_FILE_PATH = BASE_DIR + "usersXML.xml";

    public final static File USER_XML_FILE = new File(USER_XML_FILE_PATH);

    public final static File PREFERENCE_FILE = new File(BASE_DIR + "prefs.xml");

    public final static File INSTRUCTIONS_FILE = new File(RESOURCE_DIRECTORY + "instructions.txt");

    /**
     The Log Archive folder name.
     */
    public static final String LOG_FOLDER_NAME = "LogArchive";

    /**
     Path to the Log Archive folder.
     */
    public static final String LOG_FOLDER_PATH = BASE_DIR + "LogArchive";

    /**
     Directory separator character.
     */
    public static final char SEPERATOR = File.separatorChar;

    /**
     The newline character.
     */
    public static final char NEWLINE = '\n';

    /**
     Preference Key name for the COM Port.
     */
    public static final String COM_PORT_KEY = "COM_PORT";

    /**
     Preference Key name for the testing speed.
     */
    public static final String TEST_SPEED_DELAY_KEY = "TESTING_SPEED";

    /**
     Path to the serial number file.
     */
    public static final String SERIAL_PATH = RESOURCE_DIRECTORY + "serial.sn";

    /**
     Blank space character for use in the serial number file.
     */
    public static final String SERIAL_SPACE_STRING = "20";

    /**

     */
    public static final String SERIAL_BLANK_SPACE = "' '";

    /**
     The character symbol for each letter in the serial file.
     */
    public static final char SERIAL_CHAR = '\'';

    public static final String PROGRAMMING_ARGS = "/part PIC16F628A /progname PRESTO /q1";

    public static final Color GREEN = new Color(-16728503);
}
