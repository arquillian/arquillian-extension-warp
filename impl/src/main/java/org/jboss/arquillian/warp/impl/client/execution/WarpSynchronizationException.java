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

import java.util.LinkedList;
import java.util.List;

import org.jboss.arquillian.warp.client.filter.Request;
import org.jboss.arquillian.warp.exception.ClientWarpExecutionException;

/**
 * Indicates that Warp wasn't able to enrich all specified requests or didn't get response for some requests as specified by
 * user.
 *
 * @author Lukas Fryc
 */
public class WarpSynchronizationException extends ClientWarpExecutionException {

    private static final long serialVersionUID = -9086096239081792164L;

    private String message;

    public WarpSynchronizationException(WarpContext context) {
        this.message = new MessageGenerator(context).generateMessage();
    }

    @Override
    public String getMessage() {
        return message;
    }

    private static class MessageGenerator {

        private WarpContext context;
        private StringBuilder message = new StringBuilder();

        public MessageGenerator(WarpContext context) {
            this.context = context;
        }

        public String generateMessage() {
            generalIntroMessage();
            messageForRequestWithoutResponse();
            messageForUnsatisfiedObservers();
            messageForUnmatchedRequests();
            generalAdviceForDebugging();

            return message.toString();
        }

        private void generalIntroMessage() {
            message.append("The Warp failed to observe requests or match them with response.\n\n");
        }

        private void messageForRequestWithoutResponse() {
            List<Request> requestsWithoutResponse = new LinkedList<Request>();
            for (WarpGroup group : context.getAllGroups()) {
                requestsWithoutResponse.addAll(group.getRequestsWithoutResponse());
            }
            if (!requestsWithoutResponse.isEmpty()) {
                message.append("The warped request timed out when waiting for a response for following requests: ");
                message.append(requestsWithoutResponse);
                message.append("\n\n");
            }
        }

        private void messageForUnsatisfiedObservers() {
            List<WarpGroup> failedGroups = new LinkedList<WarpGroup>();
            for (WarpGroup group : context.getAllGroups()) {
                if (group.getHitCount() < group.getExpectedRequestCount()) {
                    failedGroups.add(group);
                }
            }

            if (failedGroups.size() == 1 && failedGroups.get(0).getExpectedRequestCount() == 1) {
                message.append(String.format("There were no requests matched by observer [%s]\n\n", failedGroups.get(0)
                    .getObserver()));
            } else if (failedGroups.size() >= 1) {
                message.append("There were wrong number of requests matched by observers:\n");
                for (WarpGroup group : failedGroups) {
                    message.append(String.format(" - [%s]: %s requests expected, %s done", group.getObserver(),
                        group.getExpectedRequestCount(), group.getHitCount()));
                }
                message.append("\n\n");
            }
        }

        private void messageForUnmatchedRequests() {
            List<Request> unmatchedRequests = context.getUnmatchedRequests();

            if (!unmatchedRequests.isEmpty()) {
                message.append(
                    String.format("The %s other request/-s were noticed by Warp for the given activity: %s\n\n",
                        unmatchedRequests.size(), unmatchedRequests));
            }
        }

        private void generalAdviceForDebugging() {
            message.append(
                "If Warp enriched a wrong request, use observe(...) method to select appropriate request which should be enriched instead.\n");
            message.append(
                "Otherwise check the server-side log and enable Arquillian debugging mode on both, test and server VM by passing -Darquillian.debug=true.\n");
        }
    }
}
