/**
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

import javax.faces.FacesException;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.context.FacesContextWrapper;
import javax.faces.lifecycle.Lifecycle;
import javax.servlet.http.HttpServletRequest;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.warp.spi.LifecycleManager;
import org.jboss.arquillian.warp.spi.LifecycleManagerStore;
import org.jboss.arquillian.warp.spi.WarpCommons;
import org.jboss.arquillian.warp.spi.exception.ObjectAlreadyAssociatedException;
import org.jboss.arquillian.warp.spi.exception.ObjectNotAssociatedException;

public class FacesContextFactoryWrapper extends FacesContextFactory {

    public static final String INITIALIZED = FacesContextFactoryWrapper.class.getName() + ".INITIALIZED";

    private FacesContextFactory delegate;

    public FacesContextFactoryWrapper(FacesContextFactory facesContextFactory) {
        delegate = facesContextFactory;
    }

    public FacesContext getFacesContext(Object context, Object request, Object response, Lifecycle lifecycle)
            throws FacesException {

        FacesContext facesContext = new WrappedFacesContext(delegate.getFacesContext(context, request, response, lifecycle));

        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpReq = (HttpServletRequest) request;

            @SuppressWarnings("unchecked")
            Instance<LifecycleManagerStore> store = (Instance<LifecycleManagerStore>) httpReq
                    .getAttribute(WarpCommons.LIFECYCLE_MANAGER_STORE_REQUEST_ATTRIBUTE);

            facesContext.getAttributes().put(INITIALIZED, Boolean.FALSE);

            if (store != null && store.get() != null) {
                try {
                    store.get().bind(FacesContext.class, facesContext);
                    facesContext.getAttributes().put(INITIALIZED, Boolean.TRUE);

                    LifecycleManager lifecycleManager = LifecycleManagerStore.get(FacesContext.class, facesContext);
                    lifecycleManager.fireEvent(new FacesContextInitialized(facesContext));
                } catch (ObjectAlreadyAssociatedException e) {
                    throw new IllegalStateException(e);
                } catch (ObjectNotAssociatedException e) {
                    throw new IllegalStateException(e);
                }
            }
        }

        return facesContext;
    }

    public class WrappedFacesContext extends FacesContextWrapper {

        private FacesContext wrapped;

        public WrappedFacesContext(FacesContext wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public FacesContext getWrapped() {
            return wrapped;
        }

        @Override
        public void release() {
            try {
                Object request = this.getExternalContext().getRequest();

                if (request instanceof HttpServletRequest) {
                    HttpServletRequest httpReq = (HttpServletRequest) request;

                    @SuppressWarnings("unchecked")
                    Instance<LifecycleManagerStore> store = (Instance<LifecycleManagerStore>) httpReq
                            .getAttribute(WarpCommons.LIFECYCLE_MANAGER_STORE_REQUEST_ATTRIBUTE);

                    if (store != null && store.get() != null) {
                        try {
                            store.get().unbind(FacesContext.class, this);
                        } catch (ObjectNotAssociatedException e) {
                            throw new IllegalStateException(e);
                        }
                    }
                }
            } finally {
                super.release();
            }
        }

    }

}