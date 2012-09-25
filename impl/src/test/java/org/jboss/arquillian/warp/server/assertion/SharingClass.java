package org.jboss.arquillian.warp.server.assertion;

import org.jboss.arquillian.warp.ServerAssertion;

@SuppressWarnings("unused")
public class SharingClass {

    public void client() {

        ClientInterface client = new ClientImplementation();

        new ServerAssertion() {
        };

        new Shared();
    }

    public class Shared extends ServerAssertion {

        public void server() {
            ServerInterface server = new ServerImplemenation();
        }
    }
}
