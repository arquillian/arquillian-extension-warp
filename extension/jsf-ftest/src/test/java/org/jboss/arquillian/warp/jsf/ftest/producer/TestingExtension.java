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

import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.el.ELContext;
import jakarta.el.ELResolver;
import jakarta.el.ExpressionFactory;
import jakarta.faces.application.Application;
import jakarta.faces.application.NavigationHandler;
import jakarta.faces.application.ResourceHandler;
import jakarta.faces.application.StateManager;
import jakarta.faces.application.ViewHandler;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.ExceptionHandler;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.Flash;
import jakarta.faces.context.PartialViewContext;
import jakarta.faces.render.RenderKit;

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
        assertNotNull(facesContext.get(), "FacesContext should be available");
        assertNotNull(elContext.get(), "ELContext should be available");
        assertNotNull(exceptionHandler.get(), "ExceptionHandler should be available");
        assertNotNull(partialViewContext.get(), "PartialViewContext should be available");
        assertNotNull(renderKit.get(), "RenderKit should be available");
        assertNotNull(viewRoot.get(), "UIViewRoot should be available");
        assertNotNull(externalContext.get(), "ExternalContext should be available");
        assertNotNull(flash.get(), "Flash should be available");
        assertNotNull(application.get(), "Application should be available");
        assertNotNull(elResolver.get(), "ELResolver should be available");
        assertNotNull(expressionFactory.get(), "ExpressionFactory should be available");
        assertNotNull(navigationHandler.get(), "NavigationHandler should be available");
        assertNotNull(resourceHandler.get(), "ResourceHandler should be available");
        assertNotNull(stateManager.get(), "StateManager should be available");
        assertNotNull(viewHandler.get(), "ViewHandler should be available");
    }
}
