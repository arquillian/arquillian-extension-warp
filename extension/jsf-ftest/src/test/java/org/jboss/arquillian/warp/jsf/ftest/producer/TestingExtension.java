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
package org.jboss.arquillian.warp.jsf.ftest.producer;

import static org.junit.Assert.assertNotNull;

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

import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.warp.jsf.PhaseLifecycleEvent;

public class TestingExtension implements RemoteLoadableExtension {

    @Override
    public void register(ExtensionBuilder builder) {
        builder.observer(this.getClass());
    }

    @Inject
    private Instance<FacesContext> facesContext;

    @Inject
    private Instance<ELContext> elContext;

    @Inject
    private Instance<ExceptionHandler> exceptionHandler;

    @Inject
    private Instance<PartialViewContext> partialViewContext;

    @Inject
    private Instance<RenderKit> renderKit;

    @Inject
    private Instance<UIViewRoot> viewRoot;

    @Inject
    private Instance<ExternalContext> externalContext;

    @Inject
    private Instance<Flash> flash;

    @Inject
    private Instance<Application> application;

    @Inject
    private Instance<ELResolver> elResolver;

    @Inject
    private Instance<ExpressionFactory> expressionFactory;

    @Inject
    private Instance<NavigationHandler> navigationHandler;

    @Inject
    private Instance<ResourceHandler> resourceHandler;

    @Inject
    private Instance<StateManager> stateManager;

    @Inject
    private Instance<ViewHandler> viewHandler;

    public void observeRenderResponse(@Observes PhaseLifecycleEvent.AfterRenderResponse event) {
        assertNotNull("FacesContext should be available", facesContext.get());
        assertNotNull("ELContext should be available", elContext.get());
        assertNotNull("ExceptionHandler should be available", exceptionHandler.get());
        assertNotNull("PartialViewContext should be available", partialViewContext.get());
        assertNotNull("RenderKit should be available", renderKit.get());
        assertNotNull("UIViewRoot should be available", viewRoot.get());
        assertNotNull("ExternalContext should be available", externalContext.get());
        assertNotNull("Flash should be available", flash.get());
        assertNotNull("Application should be available", application.get());
        assertNotNull("ELResolver should be available", elResolver.get());
        assertNotNull("ExpressionFactory should be available", expressionFactory.get());
        assertNotNull("NavigationHandler should be available", navigationHandler.get());
        assertNotNull("ResourceHandler should be available", resourceHandler.get());
        assertNotNull("StateManager should be available", stateManager.get());
        assertNotNull("ViewHandler should be available", viewHandler.get());
    }
}
