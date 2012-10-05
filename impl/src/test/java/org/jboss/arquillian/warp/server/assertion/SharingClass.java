package org.jboss.arquillian.warp.server.assertion;

import org.jboss.arquillian.warp.ServerAssertion;

@SuppressWarnings("unused")
public class SharingClass {

    public Shared client() {

        ClientInterface client = new ClientImplementation();

        return new Shared();
    }

    public static class Shared extends ServerAssertion {

        public void server() {
            ServerInterface server = new ServerImplemenation();
        }
    }
}
