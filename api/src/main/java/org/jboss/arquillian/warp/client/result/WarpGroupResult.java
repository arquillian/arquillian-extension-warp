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
package org.jboss.arquillian.warp.client.result;

import java.util.List;

import org.jboss.arquillian.warp.ServerAssertion;

/**
 * The request group verification result.
 *
 * @author Lukas Fryc
 */
public interface WarpGroupResult {

    /**
     * Returns assertion verified during first request in this group
     */
    <T extends ServerAssertion> T getAssertion();

    /**
     * Returns assertion verified during N-th request verified in this group
     */
    <T extends ServerAssertion> T getAssertionForHitNumber(int hitNumber);

    /**
     * Returns list of all assertions verified during first request in this group
     */
    List<ServerAssertion> getAssertions();

    /**
     * Returns list of all assertions verified during N-th request in this group
     */
    List<ServerAssertion> getAssertionsForHitNumber(int hitNumber);

    /**
     * Returns how many requests were verified in this group
     */
    int getHitCount();
}
