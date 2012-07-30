package org.opennms.core.utils.url;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public class GenericURLStreamHandler extends URLStreamHandler {
    private Class<URLConnection> urlConnectionClass;
    private int defaultPort = -1;


    public GenericURLStreamHandler(Class<URLConnection> urlConnectionClass, int defaultPort) {
        this.urlConnectionClass = urlConnectionClass;
        this.defaultPort = defaultPort;
    }

    public GenericURLStreamHandler(Class<URLConnection> urlConnectionClass) {
        this(urlConnectionClass, -1);
    }

    @Override
    protected int getDefaultPort() {
        return defaultPort;
    }

    @Override
    protected URLConnection openConnection(URL u) throws IOException {
        URLConnection urlConnection = null;
        try {
            Constructor constructor = urlConnectionClass
                    .getConstructor(new Class[]{URL.class});
            urlConnection = (URLConnection) constructor
                    .newInstance(new Object[]{u});
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return urlConnection;
    }

    @Override
    protected void parseURL(URL u, String spec, int start, int limit) {
        super.parseURL(u, spec, start, limit);
    }
}
