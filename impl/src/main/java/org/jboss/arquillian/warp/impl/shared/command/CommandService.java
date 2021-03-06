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
 * Performs a command remotely.
 * <p>
 * The object on the client side should be enriched with remote injection subjects (enriched fields should use transient to be
 * serializable).
 *
 * @author Lukas Fryc
 */
public interface CommandService {

    /**
     * Executes given command remotely and synchronously and returns the similar object as executed, but changed on the remote
     * side.
     */
    <T extends Command> T execute(T command);
}
