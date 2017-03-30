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

import java.io.Serializable;

/**
 * The payload with command and throwable that can be thrown when command was performed in the target destination.
 *
 * @author Lukas Fryc
 */
public class CommandPayload implements Serializable {

    private static final long serialVersionUID = 1L;

    protected Command command;
    protected Throwable throwable;
    protected boolean executed = false;

    public CommandPayload(Command command) {
        this.command = command;
    }

    public Command getCommand() {
        return command;
    }

    public void setCommand(Command command) {
        this.command = command;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setExecuted() {
        this.executed = true;
    }

    public boolean isExecuted() {
        return executed;
    }
}
