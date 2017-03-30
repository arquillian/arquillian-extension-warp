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
package org.jboss.arquillian.warp.spi.servlet.event;

/**
 * <p>
 * This event is fired as a reaction to {@link ProcessHttpRequest} for every request that should be processed by Warp.
 * </p>
 * <p>
 * <p>
 * Requests that should not be processed by Warp fires {@link ProcessHttpRequest} but their processing needs to be separated and
 * so they do not fire {@link ProcessWarpRequest}.
 * </p>
 * <p>
 * <p>
 * As a reaction to this event, Warp implementation should fire {@link ExecuteWarp} event and then watch for Warp execution
 * result - in this stage Warp guards all exception that may arise from processing and finally it waits for result of the test.
 * Finally it saves this result so that it can be retrieved by Warp Client.
 * </p>
 *
 * @see ExecuteWarp
 * @see ProcessHttpRequest
 */
public class ProcessWarpRequest {

}
