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
package org.jboss.arquillian.warp.jsf;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.faces.event.PhaseId;

import org.jboss.arquillian.warp.spi.WarpLifecycleEvent;

/**
 * The lifecycle event which binds with {@link BeforePhase} verification execution.
 *
 * @author Lukas Fryc
 *
 */
public abstract class PhaseLifecycleEvent extends WarpLifecycleEvent {

    private Phase phase;
    private When when;

    private PhaseLifecycleEvent(Phase phase, When when) {
        this.phase = phase;
        this.when = when;
    }

    @Override
    public List<Annotation> getQualifiers() {
        switch (when) {
            case BEFORE:
                return Arrays.asList((Annotation) new BeforePhase() {

                            @Override
                            public Class<? extends Annotation> annotationType () {
                                return BeforePhase.class;
                            }

                            @Override
                            public Phase value () {
                                return phase;
                            }
                        }
                );

            case AFTER:
                return Arrays.asList((Annotation) new AfterPhase() {

                            @Override
                            public Class<? extends Annotation> annotationType () {
                                return AfterPhase.class;
                            }

                            @Override
                            public Phase value () {
                                return phase;
                            }
                        }
                );
        }
        throw new IllegalStateException();
    }

    public static PhaseLifecycleEvent getInstance(PhaseId phaseId, When when) {
        if (when == When.BEFORE) {
            if (phaseId == PhaseId.APPLY_REQUEST_VALUES) {
                return new BeforeApplyRequestValues();
            }
            if (phaseId == PhaseId.INVOKE_APPLICATION) {
                return new BeforeInvokeApplication();
            }
            if (phaseId == PhaseId.PROCESS_VALIDATIONS) {
                return new BeforeProcessValidations();
            }
            if (phaseId == PhaseId.RENDER_RESPONSE) {
                return new BeforeRenderResponse();
            }
            if (phaseId == PhaseId.RESTORE_VIEW) {
                return new BeforeRestoreView();
            }
            if (phaseId == PhaseId.UPDATE_MODEL_VALUES) {
                return new BeforeUpdateModelValues();
            }
        } else {
            if (phaseId == PhaseId.APPLY_REQUEST_VALUES) {
                return new AfterApplyRequestValues();
            }
            if (phaseId == PhaseId.INVOKE_APPLICATION) {
                return new AfterInvokeApplication();
            }
            if (phaseId == PhaseId.PROCESS_VALIDATIONS) {
                return new AfterProcessValidations();
            }
            if (phaseId == PhaseId.RENDER_RESPONSE) {
                return new AfterRenderResponse();
            }
            if (phaseId == PhaseId.RESTORE_VIEW) {
                return new AfterRestoreView();
            }
            if (phaseId == PhaseId.UPDATE_MODEL_VALUES) {
                return new AfterUpdateModelValues();
            }
        }
        throw new IllegalStateException("Unsupported phaseId: " + phaseId + " when: " + when);
    }

    public static class BeforeApplyRequestValues extends PhaseLifecycleEvent {
        public BeforeApplyRequestValues() {
            super(Phase.APPLY_REQUEST_VALUES, When.BEFORE);
        }
    }

    public static class AfterApplyRequestValues extends PhaseLifecycleEvent {
        public AfterApplyRequestValues() {
            super(Phase.APPLY_REQUEST_VALUES, When.AFTER);
        }
    }

    public static class BeforeInvokeApplication extends PhaseLifecycleEvent {
        public BeforeInvokeApplication() {
            super(Phase.INVOKE_APPLICATION, When.BEFORE);
        }
    }

    public static class AfterInvokeApplication extends PhaseLifecycleEvent {
        public AfterInvokeApplication() {
            super(Phase.INVOKE_APPLICATION, When.AFTER);
        }
    }

    public static class BeforeProcessValidations extends PhaseLifecycleEvent {
        public BeforeProcessValidations() {
            super(Phase.PROCESS_VALIDATIONS, When.BEFORE);
        }
    }

    public static class AfterProcessValidations extends PhaseLifecycleEvent {
        public AfterProcessValidations() {
            super(Phase.PROCESS_VALIDATIONS, When.AFTER);
        }
    }

    public static class BeforeRenderResponse extends PhaseLifecycleEvent {
        public BeforeRenderResponse() {
            super(Phase.RENDER_RESPONSE, When.BEFORE);
        }
    }

    public static class AfterRenderResponse extends PhaseLifecycleEvent {
        public AfterRenderResponse() {
            super(Phase.RENDER_RESPONSE, When.AFTER);
        }
    }

    public static class BeforeRestoreView extends PhaseLifecycleEvent {
        public BeforeRestoreView() {
            super(Phase.RESTORE_VIEW, When.BEFORE);
        }
    }

    public static class AfterRestoreView extends PhaseLifecycleEvent {
        public AfterRestoreView() {
            super(Phase.RESTORE_VIEW, When.AFTER);
        }
    }

    public static class BeforeUpdateModelValues extends PhaseLifecycleEvent {
        public BeforeUpdateModelValues() {
            super(Phase.UPDATE_MODEL_VALUES, When.BEFORE);
        }
    }

    public static class AfterUpdateModelValues extends PhaseLifecycleEvent {
        public AfterUpdateModelValues() {
            super(Phase.UPDATE_MODEL_VALUES, When.AFTER);
        }
    }
}
