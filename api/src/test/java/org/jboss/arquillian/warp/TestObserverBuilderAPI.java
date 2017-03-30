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
package org.jboss.arquillian.warp;

import static org.jboss.arquillian.warp.client.filter.http.HttpFilters.request;

import org.jboss.arquillian.warp.client.filter.http.HttpMethod;

public class TestObserverBuilderAPI {

    private Activity activity;
    private Inspection inspection;

    /**
     * Single client activity and server inspection applied for request matching
     * given URI
     */
    public void testFilterBuilderUriNot() {
        Warp
            .initiate(activity)
            .observe(request().uri().not().endsWith(".jsf"))
            .inspect(inspection);
    }

    /**
     * Single client activity and server inspection applied for request matching
     * given HTTP method
     */
    public void testFilterBuilderMethod() {
        Warp
            .initiate(activity)
            .observe(request().method().not().equal(HttpMethod.POST))
            .inspect(inspection);
    }

    /**
     * Single client activity and server inspection applied for request not matching
     * given HTTP header
     */
    public void testFilterBuilderHeaderNot() {
        Warp
            .initiate(activity)
            .observe(request().header().not().containsValue("Accept", "application/json"))
            .inspect(inspection);
    }

    /**
     * Single client activity and server inspection applied for request matching
     * given condition
     */
    public void testFilterBuilderComplex() {
        Warp
            .initiate(activity)
            .observe(request().uri().endsWith("resource/Client/1")
                .method().equal(HttpMethod.POST)
                .header().containsValue("Cookie", "name=Client"))
            .inspect(inspection);
    }
}
