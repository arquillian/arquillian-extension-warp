package org.jboss.arquillian.jsfunitng.test;

import java.lang.annotation.Annotation;

public class BeforeServletEvent extends LifecycleEvent {

    @Override
    public Class<? extends Annotation> getAnnotation() {
        return BeforeServlet.class;
    }

}
