package org.opennms.netmgt.poller.monitors;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import org.opennms.core.utils.TimeoutTracker;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.MonitoredService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.util.Map;

public class JCifsMonitor extends AbstractServiceMonitor {

    /**
     * logging for JCifs monitor
     */
    private final Logger logger = LoggerFactory.getLogger("OpenNMS.JCifs." + JCifsMonitor.class.getName());

    /*
    * default retries
    */
    private static final int DEFAULT_RETRY = 0;

    /*
     * default timeout
     */
    private static final int DEFAULT_TIMEOUT = 3000;

    /**
     * This method queries the CIFS share.
     *
     * @param svc        the monitored service
     * @param parameters the parameter map
     * @return the poll status for this system
     */
    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {

        String domain = (String) parameters.get("domain");
        String username = parameters.containsKey("username") ? (String) parameters.get("username") : "";
        String password = parameters.containsKey("password") ? (String) parameters.get("password") : "";

        String file = (String) parameters.get("file");

        if (!file.startsWith("/")) {
            file = "/" + file;
        }

        String authenticationString = "";

        if (domain != null) {
            authenticationString += domain + ";";
        }

        authenticationString += username + ":" + password;

        boolean existence = "true".equals(parameters.get("existence"));

        TimeoutTracker tracker = new TimeoutTracker(parameters, DEFAULT_RETRY, DEFAULT_TIMEOUT);

        PollStatus serviceStatus = PollStatus.unknown();

        if (file == null) {
            return PollStatus.unknown();
        } else {
            String path = "smb://" + svc.getIpAddr() + file;

            for (tracker.reset(); tracker.shouldRetry() && !serviceStatus.isAvailable(); tracker.nextAttempt()) {

                NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(authenticationString);

                try {
                    SmbFile smbFile = new SmbFile(path, auth);

                    if ((existence && !smbFile.exists()) || (!existence && smbFile.exists())) {
                        return PollStatus.down();
                    }

                } catch (MalformedURLException exception) {
                    return PollStatus.unresponsive();
                } catch (SmbException exception) {
                    return PollStatus.unresponsive();
                }
            }
        }

        return serviceStatus;
    }
}