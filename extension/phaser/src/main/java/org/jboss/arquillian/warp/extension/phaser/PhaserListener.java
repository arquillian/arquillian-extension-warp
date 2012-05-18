package org.jboss.arquillian.warp.extension.phaser;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

import org.jboss.arquillian.warp.spi.LifecycleManager;
import org.jboss.arquillian.warp.spi.LifecycleManagerStore;
import org.jboss.arquillian.warp.spi.ObjectNotAssociatedException;

@SuppressWarnings("serial")
public class PhaserListener implements PhaseListener {

    /*
     * (non-Javadoc)
     * 
     * @see javax.faces.event.PhaseListener#beforePhase(javax.faces.event.PhaseEvent)
     */
    @Override
    public void beforePhase(PhaseEvent event) {
        executeEvents(When.BEFORE, event);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.faces.event.PhaseListener#afterPhase(javax.faces.event.PhaseEvent)
     */
    @Override
    public void afterPhase(PhaseEvent event) {
        executeEvents(When.AFTER, event);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.faces.event.PhaseListener#getPhaseId()
     */
    @Override
    public PhaseId getPhaseId() {
        return PhaseId.ANY_PHASE;
    }

    private void executeEvents(When when, PhaseEvent event) {
        FacesContext facesContext = event.getFacesContext();
        Boolean initialized = (Boolean) facesContext.getAttributes().get(PhaserFacesContextFactoryWrapper.INITIALIZED);

        if (initialized) {
            try {
                LifecycleManager manager = LifecycleManagerStore.get(FacesContext.class, event.getFacesContext());
                manager.fireLifecycleEvent(new PhaseLifecycleEvent(event.getPhaseId(), when));
            } catch (ObjectNotAssociatedException e) {
                throw new IllegalStateException(e);
            }
        }

    }
}
