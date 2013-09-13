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

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.faces.application.Application;
import javax.faces.application.NavigationHandler;
import javax.faces.application.ResourceHandler;
import javax.faces.application.StateManager;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import javax.faces.context.PartialViewContext;
import javax.faces.render.RenderKit;

import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.EventContext;
import org.jboss.arquillian.warp.jsf.PhaseLifecycleEvent.AfterRestoreView;
import org.jboss.arquillian.warp.spi.context.RequestScoped;

public class JSFInstanceProducers {

    @Inject
    @RequestScoped
    private InstanceProducer<FacesContext> facesContext;

    @Inject
    @RequestScoped
    private InstanceProducer<ELContext> elContext;

    @Inject
    @RequestScoped
    private InstanceProducer<ExceptionHandler> exceptionHandler;

    @Inject
    @RequestScoped
    private InstanceProducer<PartialViewContext> partialViewContext;

    @Inject
    @RequestScoped
    private InstanceProducer<RenderKit> renderKit;

    @Inject
    @RequestScoped
    private InstanceProducer<UIViewRoot> viewRoot;

    @Inject
    @RequestScoped
    private InstanceProducer<ExternalContext> externalContext;

    @Inject
    @RequestScoped
    private InstanceProducer<Flash> flash;

    @Inject
    @RequestScoped
    private InstanceProducer<Application> application;

    @Inject
    @RequestScoped
    private InstanceProducer<ELResolver> elResolver;

    @Inject
    @RequestScoped
    private InstanceProducer<ExpressionFactory> expressionFactory;

    @Inject
    @RequestScoped
    private InstanceProducer<NavigationHandler> navigationHandler;

    @Inject
    @RequestScoped
    private InstanceProducer<ResourceHandler> resourceHandler;

    @Inject
    @RequestScoped
    private InstanceProducer<StateManager> stateManager;

    @Inject
    @RequestScoped
    private InstanceProducer<ViewHandler> viewHandler;

    public void observesContextInitialization(@Observes FacesContextInitialized event) {
        FacesContext facesContext = event.getFacesContext();

        this.facesContext.set(facesContext);
        this.elContext.set(facesContext.getELContext());
        this.exceptionHandler.set(facesContext.getExceptionHandler());
        this.partialViewContext.set(facesContext.getPartialViewContext());
        this.externalContext.set(facesContext.getExternalContext());
        this.flash.set(facesContext.getExternalContext().getFlash());
        this.application.set(facesContext.getApplication());
        this.elResolver.set(facesContext.getApplication().getELResolver());
        this.expressionFactory.set(facesContext.getApplication().getExpressionFactory());
        this.navigationHandler.set(facesContext.getApplication().getNavigationHandler());
        this.resourceHandler.set(facesContext.getApplication().getResourceHandler());
        this.stateManager.set(facesContext.getApplication().getStateManager());
        this.viewHandler.set(facesContext.getApplication().getViewHandler());
    }

    public void observesRestoreView(@Observes EventContext<AfterRestoreView> eventContext) {
        FacesContext facesContext = this.facesContext.get();
        UIViewRoot vr = facesContext.getViewRoot();
        if (vr != null) {
            this.viewRoot.set(vr);
        }
        RenderKit rk = facesContext.getRenderKit();
        if (rk != null) {
            this.renderKit.set(rk);
        }

        eventContext.proceed();
    }
}
