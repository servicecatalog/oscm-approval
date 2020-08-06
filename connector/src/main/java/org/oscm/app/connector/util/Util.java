/********************************************************************************
 *
 * Copyright FUJITSU LIMITED 2020
 *
 *******************************************************************************/
package org.oscm.app.connector.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.log4j.Logger;

public class Util {

    private static Logger logger = Logger.getLogger(Util.getClassStatic());

    private Util() {
    }

    /**
     * Prints formatted representations of objects to system.out. Can be used to
     * dump objects recursively to log target.
     * 
     * @param o
     *            - Object to print
     * @param ps
     *            - printstream used to print the object (System.out for
     *            example).
     */
    public static void print(Object o, PrintStream ps) {
        ps.println(toString(o).replaceAll(", ", ",\n "));
    }

    /**
     * Returns formatted representations of objects.
     * 
     * @param o
     *            - Object to print
     * @return - String holding formatted representation of the object.
     */
    public static String toString(Object o) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.print(o);
        pw.flush();
        return sw.toString();
    }

    /**
     * Get the class of the caller in a static method.
     * 
     * @return class
     */
    public static Class getClassStatic() {
        class CurrentClassGetter extends SecurityManager {
            public Class getClass(int frame) {
                return getClassContext()[frame];
            }
        }
        CurrentClassGetter ccg = new CurrentClassGetter();

        return ccg.getClass(2);
    }

    public static boolean isEmpty(String text) {
        return (text != null && text.trim().length() > 0);
    }

    /**
     * Saves the content to a text file. This style of implementation throws all
     * exceptions to the caller.
     *
     * @param fileName
     * @param content
     *            string to save
     */
    public static void saveToFile(String fileName, String content)
            throws IOException {
        // declared here only to make visible to finally clause; generic
        // reference
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(fileName));
            writer.write(content);
            writer.close();
        } finally {
            // always close the streams otherwise we would leak memory.
            if (writer != null) {
                writer.close();
            }
        }
    }

    /**
     * Read content of a text file. This style of implementation throws all
     * exceptions to the caller.
     * 
     * @param fileName
     * @return
     * @throws IOException
     */
    public static String readFromFile(String fileName) throws IOException {
        StringBuffer contents = new StringBuffer();

        // declared here only to make visible to finally clause
        BufferedReader input = null;
        try {
            // use buffering, reading one line at a time
            // FileReader always assumes default encoding is OK!
            input = new BufferedReader(new FileReader(fileName));
            String line = null; // not declared within while loop
            /*
             * readLine is a bit quirky : it returns the content of a line MINUS
             * the newline. it returns null only for the END of the stream. it
             * returns an empty String if two newlines appear in a row.
             */
            while ((line = input.readLine()) != null) {
                contents.append(line);
                contents.append(System.getProperty("line.separator"));
            }
            return contents.toString();
        } finally {
            try {
                if (input != null) {
                    // flush and close both "input" and its underlying
                    // FileReader
                    input.close();
                }
            } catch (IOException e) {
                logger.warn("Could not close reader", e);
            }

        }
    }
}
