package org.jboss.arquillian.warp.impl.client.execution;

import java.util.concurrent.atomic.AtomicReference;

// TODO re-implement to avoid race-conditions (use CAS)
public class WarpContextStore {

    private static final AtomicReference<WarpContext> reference = new AtomicReference<WarpContext>();

    static void setCurrentInstance(WarpContext warpContext) {
        reference.set(warpContext);
    }

    static WarpContext getCurrentInstance() {
        return reference.get();
    }

}
