package org.oscm.app.connector.framework;

/**
 * This exception is thrown by a connector if no fcml document can be produced,
 * due to a misconfiguration , invalid XML data or the input of invalid
 * parameters through the connector interface.
 *
 * @author opetrovs
 */
public class ProcessException extends Exception {
    /**
     * No connection to the device controller
     */
    public static final int NO_CONNECTION = 0;
    /**
     * The response of a command execution could not be interpreted.
     */
    public static final int BAD_RESPONSE = 1;
    /**
     * The response contained no data.
     */
    public static final int NO_RESPONSE_DATA = 2;
    /**
     * If XML data is not valid.
     */
    public static final int VALIDATION_ERROR = 3; // statusCode FCML
    /**
     * If a misconfiguration during startup occurs
     */
    public static final int CONFIG_ERROR = 4; // ConnectorException
    /**
     * If a XSL transformation or the transformation of XML to different data
     * types fails.
     */
    public static final int TRANSFORMATION_ERROR = 5; // ConnectorException
    /**
     * If the command execution is disabled and a command is executed then the
     * class XSLCommandTransformer throws an exception with this error code. The
     * command will be discarded.
     */
    public static final int COMMAND_EXECUTION_DISABLED = 6; // 503: discarded
                                                            // (Befehl konnte
                                                            // nicht ausgef�hrt
                                                            // werden)
    /**
     * If xpath expressions could not be evaluated. Is used all over the command
     * manager and all commands.
     */
    public static final int XPATH_ERROR = 7; // 503: discarded (Befehl konnte
                                             // nicht ausgef�hrt werden)

    /**
     * The request timed out.
     */
    public static final int TIMEOUT = 8;

    /**
     * This error code is used only if no other is suitable.
     */
    public static final int ERROR = 9;

    /**
     * This error code is used to signal the fcml-proxy the usage of wrong
     * parameters through the Connector interface.
     */
    public static final int USAGE_ERROR = 10;

    /*
     * descriptionLong.put(Integer.toString(NO_CONNECTION),
     * "Keine Verbindung zum Ger�tecontroller");
     * descriptionLong.put(Integer.toString(BAD_RESPONSE),
     * "Der R�ckgabewert der Befehlsausf�hrung konnte aufgrund fehlerhafter Syntax nicht interpretiert werden"
     * ); descriptionLong.put(Integer.toString(VALIDATION_ERROR),
     * "Fehlerhafte Syntax der XML Daten");
     * descriptionLong.put(Integer.toString(TRANSFORMATION_ERROR),
     * "Fehler bei Umwandlung der XML Daten");
     * descriptionLong.put(Integer.toString(CONFIG_ERROR),
     * "Fehlerhafte Konfiguration");
     * descriptionLong.put(Integer.toString(COMMAND_EXECUTION_DISABLED),
     * "Kommando kann nicht ausgef�hrt werden da die Kommandoausf�hrung ausgeschaltet wurde"
     * );
     */

    // other exceptions that occur in chain items are wrapped in a
    // ChainException
    private Exception cause = null;

    // an error description
    private String message;

    // indicates the cause of the error
    private int errorCode;

    /**
     * Create an instance of ChainException.
     *
     * @param message
     *            an error description
     * @param errorCode
     *            indicates the cause of the error
     * @param cause
     * @see com.skytecag.xsight.fcml.FCMLStatuscodes
     */
    public ProcessException(String message, int errorCode, Exception cause) {
        this.cause = cause;
        this.message = message;
        this.errorCode = errorCode;
    }

    /**
     * Create an instance of ChainException.
     *
     * @param message
     *            an error description
     * @param errorCode
     *            indicates the cause of the error
     * @see com.skytecag.xsight.fcml.FCMLStatuscodes
     */
    public ProcessException(String message, int errorCode) {
        this.message = message;
        this.errorCode = errorCode;
    }

    /**
     * Indicates the cause of the error.
     *
     * @return the errorcode
     */
    public int getErrorcode() {
        return errorCode;
    }

    /**
     * Returns the wrapped exception.
     *
     * @return the wrapped exception
     */
    @Override
    public Exception getCause() {
        return cause;
    }

    /**
     * Creates a string representation of this exception.
     *
     * @return the returncode, the wrapped exception and all messages in one
     *         string
     */
    @Override
    public String toString() {
        String message = "errorCode=" + errorCode + "\n";
        if (this.message != null) {
            message += this.message;
        }
        if (cause != null) {
            message += cause.getMessage() + "\n";
        }
        return message;
    }
}
