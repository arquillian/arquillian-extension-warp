package org.jboss.arquillian.warp.impl.client.proxy;

import java.net.URL;

public interface URLMapping {
    
    URL getProxyURL(URL realUrl);
}
