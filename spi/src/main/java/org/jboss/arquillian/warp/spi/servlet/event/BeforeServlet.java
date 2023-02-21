/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.warp.spi.servlet.event;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.warp.spi.WarpLifecycleEvent;
import org.jboss.arquillian.warp.spi.event.BeforeRequest;

/**
 * <p>
 * The lifecycle event which comes with {@link org.jboss.arquillian.warp.servlet.BeforeServlet} verification execution.
 * </p>
 * <p>
 * <p>
 * If you observe this event, Warp services are fully initialized and you should have access to them using {@link Inject}.
 * </p>
 * <p>
 * <p>
 * If you want to observe event right after request enters Warp servlet filter, observe {@link BeforeRequest} event instead.
 * </p>
 *
 * @author Lukas Fryc
 */
public class BeforeServlet extends WarpLifecycleEvent {

    @Override
    public List<Annotation> getQualifiers() {
        return Arrays.asList((Annotation) new org.jboss.arquillian.warp.servlet.BeforeServlet() {
                @Override
                public Class<? extends Annotation> annotationType() {
                    return org.jboss.arquillian.warp.servlet.BeforeServlet.class;
                }
            }
        );
    }
}
