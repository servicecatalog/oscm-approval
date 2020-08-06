/********************************************************************************
 *
 * Copyright FUJITSU LIMITED 2020
 *
 *******************************************************************************/
package org.oscm.app.connector.util;

import java.rmi.dgc.VMID;

/**
 * Utility class for generating unique ids.
 */
public class GUIDGenerator {
    /**
     * Generates a world wide unique id for this document. There are only two
     * constraints: An independently generated UID instance is unique over time
     * as long as the host requires more than one millisecond to reboot and its
     * system clock is never set backward.
     *
     * @see java.rmi.server.UID
     * @see java.rmi.dgc.VMID
     */
    public static synchronized String nextGUID() {
        return new VMID().toString();
    }

}
