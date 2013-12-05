package org.jboss.arquillian.warp.ftest.cdi;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;


@Named
@ApplicationScoped
public class Bean {

    public String getValue() {
        return "value";
    }
}
