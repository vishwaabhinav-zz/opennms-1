/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.rest.client;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.http.annotations.JUnitHttpServer;
import org.opennms.core.test.http.annotations.Webapp;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.rest.client.internal.JerseyClientImpl;
import org.opennms.rest.client.internal.JerseyDataLinkInterfaceService;
import org.opennms.rest.model.ClientDataLinkInterface;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations= {
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
@JUnitHttpServer(port = 10342, webapps = @Webapp(context = "/opennms", path = "src/test/resources/opennmsRestWebServices"))
public class ClientDataLinkInterfaceServiceTest {
    @Autowired
    private DatabasePopulator m_databasePopulator;
    
    private JerseyDataLinkInterfaceService m_datalinkinterfaceservice;
    
    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging(true, "DEBUG");
        m_datalinkinterfaceservice = new JerseyDataLinkInterfaceService();
        JerseyClientImpl jerseyClient = new JerseyClientImpl(
                                                         "http://127.0.0.1:10342/opennms/rest/","demo","demo");
        m_datalinkinterfaceservice.setJerseyClient(jerseyClient);
        m_databasePopulator.populateDatabase();        
    }

    @After
    public void tearDown() throws Exception {
        MockLogAppender.assertNoWarningsOrGreater();
    }
    
    @Test
    public void testLinks() throws Exception {
        
        
        
        List<ClientDataLinkInterface> datalinkinterfacelist = m_datalinkinterfaceservice.getAll();
        assertTrue(3 == datalinkinterfacelist.size());
        for (ClientDataLinkInterface datalinkinterface: datalinkinterfacelist) {
            Integer datalinkinterfaceId = datalinkinterface.getId();
            if (datalinkinterfaceId == 64) {
                checkId64(datalinkinterface);
            } else if (datalinkinterfaceId == 65) {
                checkId65(datalinkinterface);
            } else if (datalinkinterfaceId == 66 ) {
                 checkId66(datalinkinterface);
            } else {
                assertTrue(false);
            }
        }
        
        ClientDataLinkInterface datalinkinterface = m_datalinkinterfaceservice.get(64);
        checkId64(datalinkinterface);

        datalinkinterface = m_datalinkinterfaceservice.get(65);
        checkId65(datalinkinterface);

        datalinkinterface = m_datalinkinterfaceservice.get(66);
        checkId66(datalinkinterface);

        String xml = m_datalinkinterfaceservice.getXml("");
        assertTrue(xml.contains("count=\"3\""));
 
        xml = m_datalinkinterfaceservice.getXml("64");
        assertTrue(xml.contains("id=\"64\""));
 
    }

/*
 * <link id="66"><ifIndex>1</ifIndex><lastPollTime>2012-07-15T12:59:37.747+02:00</lastPollTime>
 * <linkTypeId>-1</linkTypeId>
 * <nodeId>2</nodeId><nodeParentId>1</nodeParentId>
 * <parentIfIndex>1</parentIfIndex><status>A</status></link>
 */
    private void checkId66(ClientDataLinkInterface datalinkinterface) {
        assertTrue(1==datalinkinterface.getIfIndex());
        assertTrue(2==datalinkinterface.getNodeId());
        assertTrue(1==datalinkinterface.getNodeParentId());
        assertTrue(1==datalinkinterface.getParentIfIndex());
        assertTrue(-1==datalinkinterface.getLinkTypeId());
        assertTrue("A".equals(datalinkinterface.getStatus()));
    }

    /*
     * <link id="65"><ifIndex>2</ifIndex><lastPollTime>2012-07-15T12:59:37.663+02:00</lastPollTime>
     * <linkTypeId>-1</linkTypeId>
     * <nodeId>1</nodeId><nodeParentId>1</nodeParentId>
     * <parentIfIndex>1</parentIfIndex><status>A</status></link>
     */
    private void checkId65(ClientDataLinkInterface datalinkinterface) {
        assertTrue(2==datalinkinterface.getIfIndex());
        assertTrue(1==datalinkinterface.getNodeId());
        assertTrue(1==datalinkinterface.getNodeParentId());
        assertTrue(1==datalinkinterface.getParentIfIndex());
        assertTrue(-1==datalinkinterface.getLinkTypeId());
        assertTrue("A".equals(datalinkinterface.getStatus()));
    }
/*
 * <link id="64"><ifIndex>1</ifIndex><lastPollTime>2012-07-15T12:59:37.604+02:00</lastPollTime>
 * <linkTypeId>-1</linkTypeId>
 * <nodeId>1</nodeId><nodeParentId>1</nodeParentId>
 * <parentIfIndex>1</parentIfIndex><status>A</status></link>
 */
    private void checkId64(ClientDataLinkInterface datalinkinterface) {
        assertTrue(1==datalinkinterface.getIfIndex());
        assertTrue(1==datalinkinterface.getNodeId());
        assertTrue(1==datalinkinterface.getNodeParentId());
        assertTrue(1==datalinkinterface.getParentIfIndex());
        assertTrue(-1==datalinkinterface.getLinkTypeId());
        assertTrue("A".equals(datalinkinterface.getStatus()));
    }

}
