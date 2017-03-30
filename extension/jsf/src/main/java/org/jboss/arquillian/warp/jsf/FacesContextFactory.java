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

import java.util.logging.Logger;

import javax.faces.FacesException;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextWrapper;
import javax.faces.lifecycle.Lifecycle;
import javax.servlet.http.HttpServletRequest;

import org.jboss.arquillian.warp.spi.LifecycleManager;
import org.jboss.arquillian.warp.spi.LifecycleManagerStore;
import org.jboss.arquillian.warp.spi.WarpCommons;
import org.jboss.arquillian.warp.spi.exception.ObjectAlreadyAssociatedException;
import org.jboss.arquillian.warp.spi.exception.ObjectNotAssociatedException;

public class FacesContextFactory extends javax.faces.context.FacesContextFactory {

    public static final String WARP_ENABLED = FacesContextFactory.class.getName() + ".ENABLED";

    private Logger log = WarpJSFCommons.LOG;

    private javax.faces.context.FacesContextFactory delegate;

    public FacesContextFactory(javax.faces.context.FacesContextFactory facesContextFactory) {
        delegate = facesContextFactory;
    }

    public FacesContext getFacesContext(Object context, Object request, Object response, Lifecycle lifecycle)
        throws FacesException {

        FacesContext facesContext = delegate.getFacesContext(context, request, response, lifecycle);

        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpReq = (HttpServletRequest) request;

            facesContext = new WarpFacesContext(facesContext);

            facesContext.getAttributes().put(WARP_ENABLED, Boolean.FALSE);

            try {
                LifecycleManager manager =
                    (LifecycleManager) httpReq.getAttribute(WarpCommons.WARP_REQUEST_LIFECYCLE_MANAGER_ATTRIBUTE);

                if (manager == null) {
                    log.warning("no association of manager found for this ServletRequest");
                    return facesContext;
                }

                manager.bindTo(FacesContext.class, facesContext);
                facesContext.getAttributes().put(WARP_ENABLED, Boolean.TRUE);
                facesContext.getAttributes().put(WarpJSFCommons.WARP_REQUEST_LIFECYCLE_MANAGER_ATTRIBUTE, manager);

                manager.fireEvent(new FacesContextInitialized(facesContext));
            } catch (ObjectAlreadyAssociatedException e) {
                throw new IllegalStateException(e);
            }
        }

        return facesContext;
    }

    public static class WarpFacesContext extends FacesContextWrapper {

        private FacesContext wrapped;

        public WarpFacesContext(FacesContext wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public FacesContext getWrapped() {
            return wrapped;
        }

        @Override
        public void release() {
            try {
                if ((Boolean) this.getAttributes().get(WARP_ENABLED)) {
                    WarpFacesContext context = WarpFacesContext.getInstance(this);

                    LifecycleManager manager = LifecycleManagerStore.get(FacesContext.class, context);
                    manager.unbindFrom(FacesContext.class, context);
                    context.getAttributes().remove(WarpJSFCommons.WARP_REQUEST_LIFECYCLE_MANAGER_ATTRIBUTE);
                }
            } catch (ObjectNotAssociatedException e) {
                throw new IllegalStateException(e);
            } finally {
                super.release();
            }
        }

        public static WarpFacesContext getInstance(FacesContext facesContext) {
            while (facesContext instanceof FacesContextWrapper) {
                if (facesContext instanceof WarpFacesContext) {
                    return (WarpFacesContext) facesContext;
                } else {
                    facesContext = ((FacesContextWrapper) facesContext).getWrapped();
                }
            }

            throw new IllegalArgumentException("provided FacesContext does not wrap WarpFacesContext");
        }
    }
}