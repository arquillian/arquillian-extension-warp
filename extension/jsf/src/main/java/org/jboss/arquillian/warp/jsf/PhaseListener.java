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

import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PhaseEvent;
import jakarta.faces.event.PhaseId;

import org.jboss.arquillian.warp.jsf.FacesContextFactory.WarpFacesContext;
import org.jboss.arquillian.warp.spi.LifecycleManager;
import org.jboss.arquillian.warp.spi.LifecycleManagerStore;
import org.jboss.arquillian.warp.spi.exception.ObjectNotAssociatedException;

@SuppressWarnings("serial")
public class PhaseListener implements jakarta.faces.event.PhaseListener {

    /*
     * (non-Javadoc)
     *
     * @see jakarta.faces.event.PhaseListener#beforePhase(jakarta.faces.event.PhaseEvent)
     */
    @Override
    public void beforePhase(PhaseEvent event) {
        executeEvents(When.BEFORE, event);
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.faces.event.PhaseListener#afterPhase(jakarta.faces.event.PhaseEvent)
     */
    @Override
    public void afterPhase(PhaseEvent event) {
        executeEvents(When.AFTER, event);
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.faces.event.PhaseListener#getPhaseId()
     */
    @Override
    public PhaseId getPhaseId() {
        return PhaseId.ANY_PHASE;
    }

    private void executeEvents(When when, PhaseEvent event) {
        FacesContext facesContext = event.getFacesContext();
        Boolean initialized = (Boolean) facesContext.getAttributes().get(FacesContextFactory.WARP_ENABLED);

        if (initialized) {
            try {
                LifecycleManager manager =
                    LifecycleManagerStore.get(FacesContext.class, WarpFacesContext.getInstance(event.getFacesContext()));
                manager.fireEvent(PhaseLifecycleEvent.getInstance(event.getPhaseId(), when));
            } catch (ObjectNotAssociatedException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
