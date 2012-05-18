package org.jboss.arquillian.warp.execution;

import java.util.HashMap;

import org.littleshoot.proxy.HttpFilter;

public class ResponseFilterMap extends HashMap<String, HttpFilter> {

    private static final long serialVersionUID = -8290386846314981260L;

    public ResponseFilterMap(String hostPort) {
        this.put(hostPort, new ResponseDeenrichmentFilter());
    }

}
