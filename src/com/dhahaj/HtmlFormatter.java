/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dhahaj;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

/**
 This Class extends the Formatter class to format log records from plain text
 to an HTML format.

 @author dhahaj
 */
class HtmlFormatter extends Formatter {

    /**
     <b>format</b> This method is called for every log record.

     @param record The <b>LogRecord</b> to format.
     @return String The HTML formated String of the <b>LogRecord</b>.
     */
    @Override
    public String format(LogRecord record) {
        StringBuilder stringBuilderbuf = new StringBuilder(1056);

        // Bold any levels >= WARNING
        stringBuilderbuf.append("<tr>\n<td>\n<center>");
        if (record.getLevel().intValue() >= Level.INFO.intValue()) {
            stringBuilderbuf.append("<b>").append(record.getLevel()).append("</b>");
        } else {
            stringBuilderbuf.append(record.getLevel());
        }

        stringBuilderbuf.append("</td>\n<td>\n<center>");

        // Include the user if any
        if (LogControler.USER == null) {
            stringBuilderbuf.append("N/A");
        } else {
            stringBuilderbuf.append(LogControler.USER.getName());
        }
        stringBuilderbuf.append("</td>\n<td>\n<center>").append(calcDate(record.getMillis()))
                .append(" </td>\n<td>\n<center>").append(formatMessage(record));
        stringBuilderbuf.append("\n<td>\n</tr>\n");
        return stringBuilderbuf.toString();
    }

    /**
     <b>calcDate</b> Creates a <b>Date</b> object from a milliseconds parameter.

     @param millisecs The time to convert in milliseconds.
     @return String The <b>Date</b> as a String.
     */
    private String calcDate(long millisecs) {
        SimpleDateFormat date_format = new SimpleDateFormat("MMM dd,yyyy HH:mm:ss");
        Date resultdate = new Date(millisecs);
        return date_format.format(resultdate);
    }

    /**
     <b>getHead</b> This method is called just after the handler using this
     formatter is created

     @param handler The Handler.
     @return String The HTML Header.
     */
    @Override
    public String getHead(Handler handler) {
        return "<HTML>\n<HEAD>\n" + (new Date()) + "\n</HEAD>\n<BODY>\n<PRE>\n<table width=\"100%\" border>\n  "
                + "<tr><th>Level</th><th>User</th><th>Date</th><th>Log Message</th></tr>\n";
    }

    /**
     <b>getTail</b> This method is called just after the handler using this
     formatter is closed.

     @param handler The Handler.
     @return String The HTML Footer.
     */
    @Override
    public String getTail(Handler handler) {
        return "</table>\n  </PRE></BODY>\n</HTML>\n";
    }

}
