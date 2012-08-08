package org.opennms.rest.client.internal;


import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.client.apache.ApacheHttpClient;
import com.sun.jersey.client.apache.config.DefaultApacheHttpClientConfig;

public class JerseyClientImpl {

    Client m_client;

    DefaultApacheHttpClientConfig m_config;
    
    WebResource m_webResource;
    
    public JerseyClientImpl(String url, String username, String password) {
        
        m_config = new DefaultApacheHttpClientConfig();
        m_config.getState().setCredentials(null, null,-1, username, password);
        m_client = ApacheHttpClient.create(m_config);
        m_webResource = m_client.resource(url);
        
    }
    
    public <T> T get(Class<T> clazz,String relativePath, MultivaluedMap<String, String> queryParams) {
        return m_webResource.path(relativePath).queryParams(queryParams).accept(MediaType.APPLICATION_XML_TYPE).get(clazz);
    }

    public <T> T get(Class<T> clazz,String relativePath) {
        return m_webResource.path(relativePath).accept(MediaType.APPLICATION_XML_TYPE).get(clazz);
    }

    public String getXml(String relativePath) {
        return m_webResource.path(relativePath).accept(MediaType.APPLICATION_XML_TYPE).get(String.class);
    }
    
    public String getXml(String relativePath,MultivaluedMap<String, String> queryParams) {
        return m_webResource.path(relativePath).queryParams(queryParams).accept(MediaType.APPLICATION_XML_TYPE).get(String.class);
    }

    public String get(String relativePath) {
    	return m_webResource.path(relativePath).accept(MediaType.TEXT_PLAIN).get(String.class);
    }
    
    public String get(String relativePath,MultivaluedMap<String, String> queryParams) {
    	return m_webResource.path(relativePath).queryParams(queryParams).accept(MediaType.TEXT_PLAIN).get(String.class);
    }

}
