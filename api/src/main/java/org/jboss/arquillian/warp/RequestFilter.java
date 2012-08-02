package org.jboss.arquillian.warp;


public interface RequestFilter<T> {
    
    boolean matches(T httpRequest);
}
