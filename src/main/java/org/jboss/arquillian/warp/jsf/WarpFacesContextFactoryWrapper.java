package org.jboss.arquillian.warp.jsf;

import javax.faces.FacesException;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.context.FacesContextWrapper;
import javax.faces.lifecycle.Lifecycle;
import javax.servlet.http.HttpServletRequest;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.warp.filter.WarpFilter;
import org.jboss.arquillian.warp.lifecycle.LifecycleManagerStore;
import org.jboss.arquillian.warp.lifecycle.LifecycleManagerStore.ObjectAlreadyAssociatedException;
import org.jboss.arquillian.warp.lifecycle.LifecycleManagerStore.ObjectNotAssociatedException;

public class WarpFacesContextFactoryWrapper extends FacesContextFactory {

    public static final String INITIALIZED = WarpFacesContextFactoryWrapper.class.getName() + ".INITIALIZED";

    private FacesContextFactory delegate;

    public WarpFacesContextFactoryWrapper(FacesContextFactory facesContextFactory) {
        delegate = facesContextFactory;
    }

    public FacesContext getFacesContext(Object context, Object request, Object response, Lifecycle lifecycle)
            throws FacesException {

        FacesContext facesContext = new WrappedFacesContext(delegate.getFacesContext(context, request, response, lifecycle));

        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpReq = (HttpServletRequest) request;
            Instance<LifecycleManagerStore> store = (Instance<LifecycleManagerStore>) httpReq
                    .getAttribute(WarpFilter.LIFECYCLE_MANAGER_STORE_REQUEST_ATTRIBUTE);

            facesContext.getAttributes().put(INITIALIZED, Boolean.FALSE);

            if (store.get() != null) {
                try {
                    store.get().bind(FacesContext.class, facesContext);
                    facesContext.getAttributes().put(INITIALIZED, Boolean.TRUE);
                } catch (ObjectAlreadyAssociatedException e) {
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
                    Instance<LifecycleManagerStore> store = (Instance<LifecycleManagerStore>) httpReq
                            .getAttribute(WarpFilter.LIFECYCLE_MANAGER_STORE_REQUEST_ATTRIBUTE);

                    if (store.get() != null) {
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