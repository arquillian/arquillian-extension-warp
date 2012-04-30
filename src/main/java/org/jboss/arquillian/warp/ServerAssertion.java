/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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

import java.io.Serializable;

import org.jboss.arquillian.warp.test.AfterServlet;
import org.jboss.arquillian.warp.test.BeforeServlet;

/**
 * Provides contract for being assertion which could be serialized and sent to the server.
 * 
 * Implementations should use SPI annotations to trigger asserting logic on server, such as {@link BeforeServlet} or
 * {@link AfterServlet}.
 * 
 * @author Lukas Fryc
 * 
 */
public interface ServerAssertion extends Serializable {

}
