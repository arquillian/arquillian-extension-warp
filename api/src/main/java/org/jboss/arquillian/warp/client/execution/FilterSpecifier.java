package org.jboss.arquillian.warp.client.execution;

import org.jboss.arquillian.warp.ServerAssertion;

public interface FilterSpecifier {
    
    GroupExecutor verify(ServerAssertion assertion);
    
}
