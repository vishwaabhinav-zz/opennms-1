package org.opennms.netmgt.poller.monitors;

import jcifs.smb.SmbFile;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.dao.db.JUnitConfigurationEnvironment;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.test.context.ContextConfiguration;

import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/META-INF/opennms/emptyContext.xml"})
@JUnitConfigurationEnvironment
public class JCifsMonitorTest {

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();
    }

    @Test
    public void testPoll() throws UnknownHostException {
        MonitoredService svc = MonitorTestUtils.getMonitoredService(99, "10.123.123.123", "JCIFS");

        Map<String, Object> m = Collections.synchronizedMap(new TreeMap<String, Object>());

        JCifsMonitor jCifsMonitor = new JCifsMonitor();

        m.put("username", "user");
        m.put("password", "pass");
        m.put("domain", "dom");
        m.put("file", "/path/to/file");

        jCifsMonitor.poll(svc, m);
    }
}
