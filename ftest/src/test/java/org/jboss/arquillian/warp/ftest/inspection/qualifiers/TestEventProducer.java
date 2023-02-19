/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.arquillian.warp.ftest.inspection.qualifiers;

import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.warp.spi.LifecycleManager;
import org.jboss.arquillian.warp.spi.WarpLifecycleEvent;
import org.jboss.arquillian.warp.spi.servlet.event.BeforeServlet;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="http://community.jboss.org/people/kenfinni">Ken Finnigan</a>
 */
public class TestEventProducer implements RemoteLoadableExtension {

    @Inject
    private Instance<LifecycleManager> lifecycleManager;

    @Override
    public void register(ExtensionBuilder builder) {
        builder.observer(TestEventProducer.class);
    }

    public void fireTestEvent(@Observes BeforeServlet event, HttpServletRequest request) {
        lifecycleManager.get().fireEvent(new DummyAnnotationEvent(event));
    }

    public class DummyAnnotationEvent extends WarpLifecycleEvent {

        private BeforeServlet beforeServlet;

        public DummyAnnotationEvent(BeforeServlet event) {
            this.beforeServlet = event;
        }

        @Override
        public List<Annotation> getQualifiers() {
            List<Annotation> qualifiers = new ArrayList<Annotation>(beforeServlet.getQualifiers());

            qualifiers.add(
                new DummyAnnotation() {

                    @Override
                    public Class<? extends Annotation> annotationType() {
                        return DummyAnnotation.class;
                    }
                }
            );

            return qualifiers;
        }
    }
}
