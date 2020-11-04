/********************************************************************************
 *
 * Copyright FUJITSU LIMITED 2020
 *
 *******************************************************************************/
package org.oscm.app.approval.triggers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.oscm.app.approval.remote.BesClient;
import org.oscm.app.approval.remote.WebServiceTask;
import org.oscm.intf.TriggerService;

import org.oscm.app.connector.framework.ProcessException;

/**
 * Implements a thread which automatically approves a started trigger.
 */
public class AutoApprovalThread implements Runnable {
    private final static Logger log = LogManager.getLogger(AutoApprovalThread.class);

    private final int MAX_ATTEMPTS = 5;

    private final long WAIT_TIME_MS = 5000;

    private String orgId;

    private long triggerKey;

    public AutoApprovalThread(String orgId, long triggerKey) {
        this.orgId = orgId;
        this.triggerKey = triggerKey;
    }

    @Override
    public final void run() {
        log.debug("AutoApprovalThread.run() entered");

        for (int i = 1; i <= MAX_ATTEMPTS; i++) {
            log.debug("AutoApprovalThread.run() Auto-approval attempt: " + i + " orgId: " + orgId
                    + " triggerKey: " + triggerKey + ")");

            try {
                log.debug("AutoApprovalThread.run() Initialize trigger service");
                BesClient.runWebServiceAsOrganizationAdmin(orgId,
                        createApproveActionWSCall(TriggerService.class, triggerKey));

                break;
            } catch (Throwable t) {
                log.debug("AutoApprovalThread.run() Failed with: " + t.getMessage());
                try {
                    Thread.sleep(WAIT_TIME_MS);
                } catch (Throwable t2) {
                    log.error("AutoApprovalThread.run()", t2);
                    break;
                }

            }
        }
        log.debug("AutoApprovalThread.run() left");
    }

    <T> WebServiceTask<T> createApproveActionWSCall(Class<T> serv, final long triggerKey)
            throws Exception {
        return new WebServiceTask<T>(serv) {

            @Override
            public Object execute(T svc) throws ProcessException {
                try {
                    TriggerService trigSvc = (TriggerService) svc;
                    trigSvc.approveAction(triggerKey);
                } catch (Exception e) {
                    throw new ProcessException("Failed to approvate trigger " + e.getMessage(),
                            ProcessException.ERROR);
                }
                return WebServiceTask.RC_OK;
            }
        };
    }
}
