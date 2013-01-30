/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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

/**
 * This class is designed to be used by the service poller framework to test the availability
 * of the existence of files or directories on remote interfaces via CIFS. The class implements
 * the ServiceMonitor interface that allows it to be used along with other plug-ins by the service
 * poller framework.
 *
 * @author <a mailto:christian.pape@informatik.hs-fulda.de>Christian Pape</a>
 * @version 1.10.9
 */
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

        logger.debug("Domain: [], Username: [], Password: [], File: []");
        if (!file.startsWith("/")) {
            file = "/" + file;
            logger.debug("No root path given, add /. File to check '{}'", file);
        }

        //Build authentication string for NtlmPasswordAuthentication: syntax: domain;username:password
        String authenticationString = "";

        if (domain != null) {
            authenticationString += domain + ";";
        }

        authenticationString += username + ":" + password;
        logger.debug("NTLM authentication string is set to '{}'", authenticationString);

        boolean existence = "true".equals(parameters.get("existence"));

        TimeoutTracker tracker = new TimeoutTracker(parameters, DEFAULT_RETRY, DEFAULT_TIMEOUT);

        PollStatus serviceStatus = PollStatus.unknown();

        if (file == null) {
            logger.error("File parameter is not set.");
            return PollStatus.unknown("File parameter is not set.");
        } else {
            String path = "smb://" + svc.getIpAddr() + file;

            for (tracker.reset(); tracker.shouldRetry() && !serviceStatus.isAvailable(); tracker.nextAttempt()) {

                NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(authenticationString);

                try {
                    SmbFile smbFile = new SmbFile(path, auth);
                    boolean smbFileExists = smbFile.exists();

                    /*
                     * existence = true, smbFile.exists = true --> UP
                     * existence = true, smbFile.exists = false --> DOWN
                     * existence = false, smbFile.exists = true --> DOWN
                     * existence = false, smbFile.exists = false --> UP
                     */
                    if ((existence && !smbFileExists)) {
                        return PollStatus.down("File " + path + " should exist but is not available.");
                    } else {
                        if (!existence && smbFileExists) {
                            return PollStatus.down("File " + path + " should NOT exist but is available.");
                        } else {
                            return PollStatus.up();
                        }
                    }
                } catch (MalformedURLException exception) {
                    logger.error("URL exception '{}'",exception.getMessage());
                    return PollStatus.unresponsive(exception.getMessage());
                } catch (SmbException exception) {
                    logger.error("SMB exception '{}'", exception.getMessage());
                    return PollStatus.unresponsive(exception.getMessage());
                }
            }
        }
        logger.warn("Unknow error occured.");
        return serviceStatus;
    }
}