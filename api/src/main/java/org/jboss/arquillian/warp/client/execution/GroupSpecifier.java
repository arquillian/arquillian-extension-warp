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
package org.jboss.arquillian.warp.client.execution;

public interface GroupSpecifier<R> {
    /**
     * Specifies anonymous group of execution - each specified group will be independently filtered and executed, providing
     * interface for verifying different assertions for several requests caused by single client action.
     * 
     * After execution, the details of execution can be retrieved for each group independently by the sequence number given by
     * the order of definition (starting with 0). For result retrival by names, see {@link #group(Object)}.
     * 
     * @return the group executor which specifies what assertions to verify on the server
     */
    R group();

    /**
     * Specifies named group of execution - each specified group will be independently filtered and executed, providing
     * interface for verifying different assertions for several requests caused by single client action.
     * 
     * After execution, the details of execution can be retrieved for each group independently by the provided identified.
     * 
     * @return the group executor which specifies what assertions to verify on the server
     */
    R group(Object identifier);
}
