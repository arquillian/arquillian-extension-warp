package org.jboss.arquillian.warp.client.result;

import java.util.List;

import org.jboss.arquillian.warp.ServerAssertion;
import org.jboss.arquillian.warp.client.filter.RequestFilter;

public interface ResponseGroup {

    RequestFilter<?> getFilter();

    <T extends ServerAssertion> T getAssertion();
    
    <T extends ServerAssertion> T getAssertionForHitNumber(int hitNumber);
    
    List<ServerAssertion> getAssertions();
    
    List<ServerAssertion> getAssertionsForHitNumber(int hitNumber);
    
    int getHitCount();
}
