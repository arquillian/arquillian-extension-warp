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
package org.jboss.arquillian.warp.impl.shared.inspection;

import java.io.Serializable;

import org.jboss.arquillian.warp.Inspection;
import org.jboss.arquillian.warp.impl.shared.RequestPayload;

@SuppressWarnings({ "unused", "serial" })
public class SharingClass implements Serializable {

    public RequestPayload getStaticInnerClass() {

        ClientInterface client = new ClientImplementation();

        return new RequestPayload(new StaticInnerClass());
    }

    public RequestPayload getInnerClass() {

        ClientInterface client = new ClientImplementation();

        return new RequestPayload(new InnerClass());
    }

    public RequestPayload getAnonymousClass() {

        ClientInterface client = new ClientImplementation();

        return new RequestPayload(new Inspection() {
            private static final long serialVersionUID = 3294416387621240244L;

            public void server() {
                ServerInterface server = new ServerImplemenation();
            }
        });
    }

    public class InnerClass extends Inspection {

        private static final long serialVersionUID = 3567090624047097466L;

        public void server() {
            ServerInterface server = new ServerImplemenation();
        }
    }

    public static class StaticInnerClass extends Inspection {

        private static final long serialVersionUID = -715340985607368182L;

        public void server() {
            ServerInterface server = new ServerImplemenation();
        }
    }
}
