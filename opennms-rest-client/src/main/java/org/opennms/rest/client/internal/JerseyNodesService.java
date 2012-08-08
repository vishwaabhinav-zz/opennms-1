package org.opennms.rest.client.internal;

import javax.ws.rs.core.MultivaluedMap;

import org.opennms.rest.client.NodesService;
import org.opennms.rest.model.ClientOnmsNode;
import org.opennms.rest.model.ClientOnmsNodeList;

public class JerseyNodesService extends JerseyAbstractService implements NodesService {

    private static String NODES_REST_PATH = "nodes/";

    private JerseyClientImpl m_jerseyClient;
        
    public JerseyClientImpl getJerseyClient() {
        return m_jerseyClient;
    }

    public void setJerseyClient(JerseyClientImpl jerseyClient) {
        m_jerseyClient = jerseyClient;
    }

    public ClientOnmsNodeList getAll() {
    	MultivaluedMap<String, String> queryParams = setLimit(0);
        return getJerseyClient().get(ClientOnmsNodeList.class, NODES_REST_PATH,queryParams);                
    }
 
    public ClientOnmsNodeList find(MultivaluedMap<String, String> queryParams) {
        return getJerseyClient().get(ClientOnmsNodeList.class, NODES_REST_PATH,queryParams);                
    }

    public ClientOnmsNode get(Integer id) {
        return getJerseyClient().get(ClientOnmsNode.class, NODES_REST_PATH+id);
    }
 

	public ClientOnmsNodeList getWithDefaultsQueryParams() {
        return getJerseyClient().get(ClientOnmsNodeList.class, NODES_REST_PATH);                
	}
}
