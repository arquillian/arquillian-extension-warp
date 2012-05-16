package org.jboss.arquillian.warp.extensions.phaser;

public enum Phase {

    RESTORE_VIEW,
    APPLY_REQUEST_VALUES,
    PROCESS_VALIDATIONS,
    UPDATE_MODEL_VALUES,
    INVOKE_APPLICATION,
    RENDER_RESPONSE

}
