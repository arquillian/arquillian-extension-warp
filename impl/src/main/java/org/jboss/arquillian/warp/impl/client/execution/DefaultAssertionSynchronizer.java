package org.jboss.arquillian.warp.impl.client.execution;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.warp.impl.client.scope.WarpExecutionScoped;
import org.jboss.arquillian.warp.impl.shared.ResponsePayload;

public class DefaultAssertionSynchronizer implements AssertionSynchronizer {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    
    @Inject @WarpExecutionScoped
    private InstanceProducer<FutureTask<ResponsePayload>> payloadFuture;
    
    @Override
    public void advertise() {
        AssertionHolder.advertise();
        AssertionHolder.setExpectedRequests(1);
    }

    @Override
    public void addEnrichment(RequestEnrichment enrichment) {
        AssertionHolder.addRequest(enrichment);
    }

    @Override
    public void finish() {
        AssertionHolder.finished();
        
        FutureTask<ResponsePayload> future = new FutureTask<ResponsePayload>(new PushAssertion());
        payloadFuture.set(future);
        executor.submit(future);
    }

    @Override
    public ResponsePayload waitForResponse() {
        ResponsePayload responsePayload;

        try {
            responsePayload = payloadFuture.get().get();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        
        return responsePayload;
    }

    @Override
    public void clean() {
        AssertionHolder.finishEnrichmentRound();
    }
    
    public class PushAssertion implements Callable<ResponsePayload> {
        @Override
        public ResponsePayload call() throws Exception {
            Set<ResponseEnrichment> responses = AssertionHolder.getResponses();
            ResponseEnrichment response = responses.iterator().next();
            return response.getPayload();

        }
    }
    
}
