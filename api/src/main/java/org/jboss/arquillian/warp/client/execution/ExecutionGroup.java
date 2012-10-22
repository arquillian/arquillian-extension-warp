package org.jboss.arquillian.warp.client.execution;

import org.jboss.arquillian.warp.client.filter.RequestFilter;

public interface ExecutionGroup {
    
    FilterSpecifier filter(RequestFilter<?> filter);
    
}
