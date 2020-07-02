package com.dhahaj;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.*;
import static processing.core.PApplet.println;
import static com.dhahaj.UsersXML.User;

/**
 <b>LogControler</b> Handles logging of data and maintaining an archive of
 logs.

 @author dhahaj
 */
public class LogControler implements Constants {

    static private FileHandler fileTxt;
    static private SimpleFormatter formatterTxt;

    static private FileHandler fileHTML;
    static private Formatter formatterHTML;
    static public String FilePath;
    static public boolean Append = true;
    //  static public String User = null;
    public static boolean SAVE_TEXT_LOG = true;
    public static boolean SAVE_HTML_LOG = true;
    public static User USER = null;
    //  private static final Logger LOGGER = Logger.getLogger(GUIFrame.class.getName());
    static final Logger logger = Logger.getLogger(Main.class.getName());
    private static boolean started = false;

    private LogControler() {
    }

    /**
     <b>setup</b> Initializes the logger/FileHandlers/formatter, and creates
     both the text and html log files.

     @throws IOException IOException
     */
    static public void setup() throws IOException {
        if (started) {
            println("Logger started twice.");
            return;
        }
        checkDate(FilePath);
        checkFolder();

        // Create Logger
        logger.setLevel(Level.FINE);

        // Create the FileHandlers
        fileTxt = new FileHandler(FilePath + ".txt", Append);
        fileHTML = new FileHandler(FilePath + ".html", Append);

        // Create txt Formatter
        formatterTxt = new SimpleFormatter();
        fileTxt.setFormatter(formatterTxt);
        logger.addHandler(fileTxt);

        // Create HTML Formatter
        formatterHTML = new HtmlFormatter();
        fileHTML.setFormatter(formatterHTML);
        logger.addHandler(fileHTML);
        started = true;
    }

    /**
     Checks the modified date of latest log to the current date. If they are
     different it renames the log file and relocates it to an archive folder.

     @param FilePath The path to the current log file.
     */
    @SuppressWarnings("deprecation")
    private static void checkDate(String FilePath) {

        // Check the dates to see if the logs should be archived
        Date lastModified = new Date(new File(FilePath + ".txt").lastModified());
        Calendar rightNow = Calendar.getInstance();
        Date now = rightNow.getTime();
        System.out.println("modified: " + lastModified.getDate() + " now: " + now.getDate());

        if (now.getDate() == lastModified.getDate()) {
            return;
        }
        println("Archiving log files.");
        // Paths for the text and html files to be archived to
        final String textFilePath = (FilePath + ".txt");
        final String htmlFilePath = (FilePath + ".html");

        // Handle the text formatted log.
        File textFile = new File(textFilePath);
        final String absPath = textFile.getAbsolutePath();
        final String newBasePath = absPath.substring(0, absPath.lastIndexOf(File.separator)) + File.separator
                + LOG_FOLDER_NAME;
        final String newname = "Log_" + (lastModified.getMonth() + 1) + "." + lastModified.getDate() + "."
                + (lastModified.getYear() + 1900);

        // Create the new text file
        String newFileName = newBasePath + File.separator + newname + ".txt";
        int n = 1;
        while (fileExists(newFileName)) {
            newFileName = newBasePath + File.separator + newname + "_" + n + ".txt";
            n++;
        }

        boolean ok, archived;
        if (SAVE_TEXT_LOG) {
            File newTextFile = new File(newFileName);
            ok = textFile.renameTo(newTextFile);

            archived = archiveFile(newTextFile);
            if (!archived || !ok) {
                System.err.println("Error archiving file: " + newTextFile);
            }
        }

        if (SAVE_HTML_LOG) {
            // Create the new html file
            File htmlFile = new File(htmlFilePath);
            File newHtmlFile = new File(newFileName.replaceAll(".txt", ".html"));
            ok = htmlFile.renameTo(newHtmlFile);

            archived = archiveFile(newHtmlFile);
            if (!archived || !ok) {
                System.err.println("Error archiving file: " + newHtmlFile);
            }
        }

    }

    private static boolean archiveFile(File file) {
        FileWriter out = null;
        boolean b = false;
        try {
            out = new FileWriter(file, true);
            b = true;
        } catch (IOException e) {
            try {
                out = new FileWriter(file, false);
                b = true;
            } catch (IOException e1) {
            }
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
            }
        }
        return b;
    }

    /**
     <b>fileExists</b> Checks for the existence of a file.

     @param filename The filename in String format.
     @return boolean True if the file exists, False otherwise.
     */
    public static boolean fileExists(String filename) {
        FileInputStream stream = null;
        try {
            File file = new File(filename);
            stream = new FileInputStream(file);
            // The inputstream will throw an exception if the file does not
            // exist. Hence, if we get here, the file exists.
            return true;
        } catch (FileNotFoundException e) {
            return false;
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException ex) {
                ex.getMessage();
            }
        }
    }

    /**
     <b>fileExists</b> Checks for the existence of a file.

     @param f The file to check.
     @return True if file is found, False otherwise.
     */
    public static boolean fileExists(File f) {
        return fileExists(f.getAbsolutePath());
    }

    static Logger getLogger() {
        return logger;
    }

    private static void checkFolder() {
        File f = new File(LOG_FOLDER_PATH);
        if (!f.exists()) {
            try {
                Path p = Files.createDirectory(Paths.get(LOG_FOLDER_PATH));
            } catch (IOException ex) {
            }
        }
    }

}

///**
// * Log handler Class
// *
// * @author Daniel
// */
//import java.io.*;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.logging.*;
//import static processing.core.PApplet.println;
//
///**
// * <b>LogControler</b>: Handles logging of data and maintaining an archive of
// * logs.
// *
// * @author dhahaj
// */
//public class LogControler {
//
//  static private FileHandler textHandler;
//  static private SimpleFormatter textFormatter;
//
//  static private FileHandler htmlHandler;
//  static private Formatter htmlFormatter;
//
//  /**
//   * Path for the log files.
//   */
//  static public String LOGFILE;
//  static public boolean APPEND = true;
//  static public String USER = null;
//  static public String LOG_FOLDER_NAME = "LogArchive";
//  public static boolean SAVE_TEXT_LOG = true;
//  public static boolean SAVE_HTML_LOG = true;
//
//  private static final Logger myLogger = Logger.getLogger(GUIFrame.class.getName());
////  private static final Logger LOGGER = Logger.getLogger(GUIFrame.class.getName());
//
//  private LogControler() {
//  }
//
//  public static Logger getLogger() {
//    return myLogger;
//  }
//
//  /**
//   * <b>setup</b> Initializes the logger/FileHandlers/formatter, and creates
//   * both the text and html log files.
//   *
//   * @throws IOException
//   */
//  static public void setup() throws IOException {
//
//    checkDate(LOGFILE);
//
//    // Create Logger
//    myLogger.setLevel(Level.FINE);
//
//    // Create the FileHandlers
//    textHandler = new FileHandler(LOGFILE + ".txt", APPEND);
//    htmlHandler = new FileHandler(LOGFILE + ".html", APPEND);
//
//    // Create txt Formatter
//    textFormatter = new SimpleFormatter();
//    textHandler.setFormatter(textFormatter);
//    myLogger.addHandler(textHandler);
//
//    // Create HTML Formatter
//    htmlFormatter = new HtmlFormatter();
//    htmlHandler.setFormatter(htmlFormatter);
//    myLogger.addHandler(htmlHandler);
//  }
//
//  /**
//   * Checks the modified date of latest log to the current date. If they are
//   * different it renames the log file and relocates it to an archive folder.
//   *
//   * @param FilePath The path to the current log file.
//   */
//  @SuppressWarnings("deprecation")
//  private static void checkDate(String FilePath) {
//
//    myLogger.entering(LogControler.class.getName(), "checkDate(String)");
//
//    File logFile = new File(FilePath + ".txt");
//
//    // Check the dates to see if the logs should be archived
//    Date lastModified = new Date(logFile.lastModified());
//    Date now = Calendar.getInstance().getTime();
//    println("modified: " + lastModified.getDate() + " now: " + now.getDate());
//
//    if (now.getDate() < lastModified.getDate()) {
//      return;
//    }
//
//    // Paths for the text and html files to be archived to
//    final String textFilePath = (FilePath + ".txt");
//    final String htmlFilePath = (FilePath + ".html");
//
//    Path tempTextPath = null, tempHTMLPath;
//    try {
//      tempTextPath = Files.createTempFile("textLog", "tmp");
//      tempHTMLPath = Files.createTempFile("htmlLog", "tmp");
//      println(tempTextPath.toFile().getAbsolutePath());
//    } catch (IOException ex) {
//      Logger.getLogger(LogControler.class.getName()).log(Level.SEVERE, null, ex);
//    }
//
//    // Handle the text formatted log.
////    Files.copy(tempTextPath, FilePath, );
//    File textFile = new File(textFilePath);
//    final String absPath = textFile.getAbsolutePath();
//    final String newBasePath = absPath.substring(0, absPath.lastIndexOf(File.separator)) + File.separator
//            + LOG_FOLDER_NAME;
//    final String newname = "Log_" + (lastModified.getMonth() + 1) + "." + lastModified.getDate() + "."
//            + (lastModified.getYear() + 1900);
//
//    // Create the new text file
//    String newFileName = newBasePath + File.separator + newname + ".txt";
//    int n = 1;
//
//    while (fileExists(newFileName)) {
//      newFileName = newBasePath + File.separator + newname + "_" + n + ".txt";
//      n++;
//    }
//
//    boolean ok, archived;
//    if (SAVE_TEXT_LOG) {
//      File newTextFile = new File(newFileName);
//      ok = textFile.renameTo(newTextFile);
//
//      archived = archiveFile(newTextFile);
//      if (!archived || !ok) {
//        System.err.println("Error archiving file: " + newTextFile);
//      }
//    }
//
//    if (SAVE_HTML_LOG) {
//      // Create the new html file
//      File htmlFile = new File(htmlFilePath);
//      File newHtmlFile = new File(newFileName.replaceAll(".txt", ".html"));
//      ok = htmlFile.renameTo(newHtmlFile);
//
//      archived = archiveFile(newHtmlFile);
//      if (!archived || !ok) {
//        System.err.println("Error archiving file: " + newHtmlFile);
//      }
//    }
//  }
//
//  /**
//   * Archives old log files.
//   *
//   * @param file
//   * @return
//   */
//  private static boolean archiveFile(File file) {
//    FileWriter out = null;
//    boolean b = false;
//    try {
//      out = new FileWriter(file, true);
//      b = true;
//    } catch (IOException e) {
//      try {
//        out = new FileWriter(file, false);
//        b = true;
//      } catch (IOException e1) {
//      }
//    } finally {
//      try {
//        if (out != null) {
//          out.close();
//        }
//      } catch (IOException e) {
//      }
//    }
//    return b;
//  }
//
//  /**
//   * <b>fileExists</b> Checks for the existence of a file.
//   *
//   * @param filename The filename in String format.
//   * @return boolean True if the file exists, False otherwise.
//   */
//  public static boolean fileExists(String filename) {
//    boolean found = false;
//    FileInputStream stream = null;
//    try {
//      File file = new File(filename);
//      stream = new FileInputStream(file);
//      // The inputstream will throw an exception if the file does not
//      // exist. Hence, if we get here, the file exists.
//      return true;
//    } catch (FileNotFoundException e) {
//      found = false;
//      return found;
//    } finally {
//      try {
//        if (stream != null) {
//          stream.close();
//        }
//      } catch (IOException ex) {
//        ex.getMessage();
//      }
//    }
//  }
//
//  /**
//   * <b>fileExists</b> Checks for the existence of a file.
//   *
//   * @param f The file to check.
//   * @return True if file is found, False otherwise.
//   */
//  public static boolean fileExists(File f) {
//    return fileExists(f.getAbsolutePath());
//  }
//
//  /**
//   * <b>fileExists</b> Checks for the existence of a file.
//   *
//   * @param path Path for the <b>File</b> to check.
//   * @return True if file is found, False otherwise.
//   */
//  public static boolean fileExists(Path path) {
//    return fileExists(path.toFile().getAbsolutePath());
//  }
//
//}
