package org.jboss.arquillian.warp.impl.shared;

import java.io.Serializable;

public interface RemoteOperation extends Serializable {

    void execute();
}
