package org.jboss.arquillian.jsfunitng.test;

import java.lang.annotation.Annotation;

public abstract class LifecycleEvent {

    public abstract Class<? extends Annotation> getAnnotation();
}
