package org.oscm.app.connector.framework;

import java.util.Map;

/**
 * This interface is implemented by the first chain item of a chain. A connector
 * keeps a reference to this interface in order to issue command requests and
 * get a command response back.
 * <p>
 * A chain links together an arbitrary number of chain items. When a command
 * request or data retrieval process is passed down the chain through every
 * chain item, each of the chain items get a grip on the data to transform it
 * and pass it further down the chain to the next chain item. The last chain
 * item communicates with a device or controller to execute the command or
 * request data and passes the response data up the chain through every chain
 * item. An again every chain item can transform the data. All errors that occur
 * in the chain are wrapped in a ChainException.
 */
public interface IActivity {
    /**
     * Configure the chain. These properties are passed down the chain. Every
     * chain item filters it's relevant configuration parameters. Missing
     * configuration parameters cause a ConnectorException.
     *
     * @param props
     *            configuration parameters
     */
    public void setConfiguration(java.util.Properties props)
            throws ProcessException;

    /**
     * Synchronously issues a command request on a chain.
     *
     * @param transmitData
     *            contains infos from the analyzed fcml command request
     * @return the fcml command response document
     */
    public Map<String, String> transmitReceiveData(
            Map<String, String> transmitData) throws ProcessException;

}
