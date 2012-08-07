package org.opennms.rest.client;

import javax.ws.rs.core.MultivaluedMap;

import org.opennms.rest.model.ClientDataLinkInterface;
import org.opennms.rest.model.ClientDataLinkInterfaceList;

public interface DataLinkInterfaceService extends FilterService{

    public int countAll();

    public ClientDataLinkInterfaceList getAll();

    public ClientDataLinkInterfaceList getWithDefaultsQueryParams();

    public ClientDataLinkInterfaceList find(MultivaluedMap<String, String> queryParams);

    public ClientDataLinkInterface get(Integer id);
    
    

}
