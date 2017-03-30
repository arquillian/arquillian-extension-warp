/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.warp.impl.client.execution;

import org.jboss.arquillian.warp.client.filter.Request;
import org.jboss.arquillian.warp.exception.ClientWarpExecutionException;


/**
 * Indicates that Warp has observed more requests in given group than expected.
 *
 * @author Lukas Fryc
 */
public class TooManyRequestsException extends ClientWarpExecutionException {
    private static final long serialVersionUID = -9086096239081792164L;

    private String message;

    public TooManyRequestsException(WarpGroup group, Request request) {
        this.message = new MessageGenerator(group, request).generateMessage();
    }

    @Override
    public String getMessage() {
        return message;
    }

    private static class MessageGenerator {

        private WarpGroup group;
        private Request request;
        private StringBuilder message = new StringBuilder();

        public MessageGenerator(WarpGroup context, Request request) {
            this.group = context;
            this.request = request;
        }

        public String generateMessage() {
            generalIntroMessage();
            messageListingRequests();
            messageForObserver();
            generalAdviceForDebugging();

            return message.toString();
        }

        private void messageForObserver() {
            message.append("Observer: ");
            message.append(group.getObserver());
            message.append("\n\n");
        }

        private void generalIntroMessage() {
            message.append(String.format("There were more requests observed (%s) then expected (%s).\n\n",
                    1 + group.getHitCount(), group.getExpectedRequestCount()));
        }

        private void messageListingRequests() {
            for (Request request : group.getAllRequests()) {
                messageForRequest(request);
            }
            messageForRequest(this.request);
            message.append("\n");
        }

        private void messageForRequest(Request request) {
            message.append(" - ");
            message.append(request);
            message.append("\n");
        }

        private void generalAdviceForDebugging() {
            message.append("If Warp observes wrong request, use observe(...) method to select appropriate request which should be enriched instead.\n");
            message.append("You can also use method observe(request().index(1)) to select just first from those requests or simply use single-request execution API (Warp.initiate(a).observe(o).inspect(i)).\n");
            message.append("Otherwise check the server-side log and enable Arquillian debugging mode on both, test and server VM by passing -Darquillian.debug=true.\n");
        }
    }
}
