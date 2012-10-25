package org.jboss.arquillian.warp.client.result;

import org.jboss.arquillian.warp.ServerAssertion;
import org.jboss.arquillian.warp.client.filter.RequestFilter;

public interface ResponseGroup {

    RequestFilter<?> getFilter();

    <T extends ServerAssertion> T getAssertion();
    
    int getHitCount();
}
