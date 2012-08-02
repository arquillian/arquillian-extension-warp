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
package org.jboss.arquillian.warp.spi;

public class WarpCommons {

    public static final String NAMESPACE = "org.jboss.arquillian.warp";
    public static final String LIFECYCLE_MANAGER_STORE_REQUEST_ATTRIBUTE = NAMESPACE + ".MANAGER_REQUEST_ATTRIBUTE";

    private static final String ENRICHMENT = "X-Arq-Enrichment";
    public static final String ENRICHMENT_REQUEST = ENRICHMENT + "-Request";
    public static final String ENRICHMENT_RESPONSE = ENRICHMENT + "-Response";

}
