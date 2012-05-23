package org.jboss.arquillian.warp.extension.phaser.ftest;

import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

public class FailingPhaseListener implements PhaseListener {

    private static final long serialVersionUID = 1L;

    @Override
    public void afterPhase(PhaseEvent event) {
        throw new TestingException();
    }

    @Override
    public void beforePhase(PhaseEvent event) {
        throw new TestingException();
    }

    @Override
    public PhaseId getPhaseId() {
        return PhaseId.ANY_PHASE;
    }

    public static class TestingException extends RuntimeException {

        private static final long serialVersionUID = 1L;

    }

}
