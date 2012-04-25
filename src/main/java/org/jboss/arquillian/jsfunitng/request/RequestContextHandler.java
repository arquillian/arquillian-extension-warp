package org.jboss.arquillian.jsfunitng.request;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.EventContext;

public class RequestContextHandler {

    @Inject
    private Instance<RequestContext> requestContextInstance;

    public void createRequestContext(@Observes(precedence = 100) EventContext<BeforeRequest> context) {
        RequestContext requestContext = this.requestContextInstance.get();
        requestContext.activate();
        context.proceed();
    }

    public void destroyRequestContext(@Observes(precedence = 100) EventContext<AfterRequest> context) {
        RequestContext requestContext = this.requestContextInstance.get();
        try {
            context.proceed();
        } finally {
            requestContext.deactivate();
        }
    }
}
