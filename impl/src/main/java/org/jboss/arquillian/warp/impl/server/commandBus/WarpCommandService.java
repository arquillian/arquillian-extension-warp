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
package org.jboss.arquillian.warp.impl.server.commandBus;

import org.jboss.arquillian.container.test.spi.command.Command;
import org.jboss.arquillian.container.test.spi.command.CommandService;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.ServiceLoader;

/**
 * This {@link CommandService} uses Warp's own command service: {@link org.jboss.arquillian.warp.impl.shared.command.CommandService} to perform an operation on the client.
 *
 * @author Lukas Fryc
 * @author Aris Tzoumas
 */
public class WarpCommandService implements CommandService {

    @Inject
    private Instance<ServiceLoader> serviceLoader;

    @Override
    public <T> T execute(Command<T> command) {
        org.jboss.arquillian.warp.impl.shared.command.CommandService remoteOperationService =
            serviceLoader.get().onlyOne(org.jboss.arquillian.warp.impl.shared.command.CommandService.class);

        @SuppressWarnings("unchecked")
        Command<T> newCommand =
            (Command<T>) remoteOperationService.execute(new FireCommandAsEventOnClient(command)).command;

        if (newCommand.getThrowable() != null) {
            throw new RuntimeException(newCommand.getThrowable());
        }
        return (T) newCommand.getResult();
    }

    public static class FireCommandAsEventOnClient implements org.jboss.arquillian.warp.impl.shared.command.Command {
        private static final long serialVersionUID = 1L;

        @Inject
        private transient Event<Object> eventExecutedOnClient;

        private Command<?> command;

        public FireCommandAsEventOnClient(Command<?> command) {
            this.command = command;
        }

        @Override
        public void perform() {
            eventExecutedOnClient.fire(command);
        }
    }
}
