/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.warp.jsf;

import java.lang.annotation.Annotation;

import javax.faces.event.PhaseId;

import org.jboss.arquillian.core.spi.Validate;
import org.jboss.arquillian.warp.test.LifecycleEvent;

/**
 * The lifecycle event which binds with {@link BeforePhase} verification execution.
 * 
 * @author Lukas Fryc
 * 
 */
public class PhaseLifecycleEvent extends LifecycleEvent {

    private Phase phase;
    private When when;

    public PhaseLifecycleEvent(PhaseId phaseId, When when) {
        Validate.notNull(phaseId, "phaseId must not be null");
        this.phase = getPhase(phaseId);
        this.when = when;
    }

    @Override
    public Annotation getAnnotation() {
        switch (when) {
            case BEFORE:
                return new BeforePhase() {

                    @Override
                    public Class<? extends Annotation> annotationType() {
                        return BeforePhase.class;
                    }

                    @Override
                    public Phase value() {
                        return phase;
                    }
                };

            case AFTER:
                return new AfterPhase() {

                    @Override
                    public Class<? extends Annotation> annotationType() {
                        return AfterPhase.class;
                    }

                    @Override
                    public Phase value() {
                        return phase;
                    }
                };
        }
        throw new IllegalStateException();
    }

    private Phase getPhase(PhaseId phaseId) {
        if (phaseId == PhaseId.APPLY_REQUEST_VALUES) {
            return Phase.APPLY_REQUEST_VALUES;
        }
        if (phaseId == PhaseId.INVOKE_APPLICATION) {
            return Phase.INVOKE_APPLICATION;
        }
        if (phaseId == PhaseId.PROCESS_VALIDATIONS) {
            return Phase.PROCESS_VALIDATIONS;
        }
        if (phaseId == PhaseId.RENDER_RESPONSE) {
            return Phase.RENDER_RESPONSE;
        }
        if (phaseId == PhaseId.RESTORE_VIEW) {
            return Phase.RESTORE_VIEW;
        }
        if (phaseId == PhaseId.UPDATE_MODEL_VALUES) {
            return Phase.UPDATE_MODEL_VALUES;
        }
        throw new UnsupportedOperationException("the phaseId '" + phaseId + "' is not supported");
    }
}
