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
package org.jboss.arquillian.warp.impl.shared.command;

/**
 * <p>
 * CommandEventBus Service Operation Modes.
 * </p>
 * <p>
 * <ul>
 * <li><code>GET</code> - A Command sent from the remote container.
 * <li><code>PUT</code> - A Command sent from client, targeted to the remote container.
 * </ul>
 *
 * @author Aris Tzoumas
 */
public enum OperationMode {
    GET,
    PUT
}
