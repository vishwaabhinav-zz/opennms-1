package org.opennms.rest.client.internal;

import javax.ws.rs.core.MultivaluedMap;

import org.opennms.rest.client.DataLinkInterfaceService;
import org.opennms.rest.model.ClientDataLinkInterface;
import org.opennms.rest.model.ClientDataLinkInterfaceList;

public class JerseyDataLinkInterfaceService extends JerseyAbstractService implements DataLinkInterfaceService {

    private static String LINK_REST_PATH = "links/";

    private JerseyClientImpl m_jerseyClient;
        
    public JerseyClientImpl getJerseyClient() {
        return m_jerseyClient;
    }

    public void setJerseyClient(JerseyClientImpl jerseyClient) {
        m_jerseyClient = jerseyClient;
    }

    public ClientDataLinkInterfaceList getAll() {
    	MultivaluedMap<String, String> queryParams = setLimit(0);
        return getJerseyClient().get(ClientDataLinkInterfaceList.class, LINK_REST_PATH,queryParams);                
    }
 
    public ClientDataLinkInterfaceList find(MultivaluedMap<String, String> queryParams) {
        return getJerseyClient().get(ClientDataLinkInterfaceList.class, LINK_REST_PATH,queryParams);                
    }

    public ClientDataLinkInterface get(Integer id) {
        return getJerseyClient().get(ClientDataLinkInterface.class, LINK_REST_PATH+id);
    }
 
	public int countAll() {
		return Integer.parseInt(getJerseyClient().get(LINK_REST_PATH+"count"));
	}

	public ClientDataLinkInterfaceList getWithDefaultsQueryParams() {
        return getJerseyClient().get(ClientDataLinkInterfaceList.class, LINK_REST_PATH);                
	}
}
