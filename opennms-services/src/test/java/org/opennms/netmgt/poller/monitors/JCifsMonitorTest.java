package org.opennms.netmgt.poller.monitors;

import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.dao.db.JUnitConfigurationEnvironment;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/META-INF/opennms/emptyContext.xml"})
@JUnitConfigurationEnvironment
public class JCifsMonitorTest {
    /*
    private final transient IMocksControl mockControl = EasyMock.createStrictControl();
    private final transient SmbFile mockSmbFileTrue = mockControl.createMock(SmbFile.class);
    private final transient SmbFile mockSmbFileFalse = mockControl.createMock(SmbFile.class);

    private class TestCase {
        public String domain;
        public String username;
        public String password;
        public String file;
        public SmbFile smbFile;
        public PollStatus result;
        public String existence;

        public TestCase(String domain, String username, String password, String file, String existence, SmbFile smbFile, PollStatus result) {
            this.domain = domain;
            this.username = username;
            this.password = password;
            this.file = file;
            this.smbFile = smbFile;
            this.result = result;
            this.existence = existence;
        }
    }

    List<TestCase> testCases = new ArrayList<TestCase>();

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();

        expect(mockSmbFileTrue.exists()).andReturn(true).anyTimes();
        expect(mockSmbFileTrue.canRead()).andReturn(true).anyTimes();

        expect(mockSmbFileFalse.exists()).andReturn(true).anyTimes();
        expect(mockSmbFileFalse.canRead()).andReturn(true).anyTimes();

        testCases.add(new TestCase("dom", "user", "pass", "", "true", mockSmbFileTrue, PollStatus.up()));

        mockControl.replay();
    }

    @Test
    public void testPoll() throws UnknownHostException {
        MonitoredService svc = MonitorTestUtils.getMonitoredService(99, "10.123.123.123", "JCIFS");

        JCifsMonitor jCifsMonitor = new JCifsMonitor();


        for (TestCase testCase : testCases) {
            Map<String, Object> m = Collections.synchronizedMap(new TreeMap<String, Object>());

            m.put("username", testCase.username);
            m.put("password", testCase.password);
            m.put("domain", testCase.domain);
            m.put("existence", testCase.existence);
            m.put("file", testCase.file);

            PollStatus pollStatus = jCifsMonitor.poll(svc, m);

            assertEquals(pollStatus, testCase.result);
        }
    }
    */
}
