package org.jboss.arquillian.warp.server.assertion;

import java.io.Serializable;

import org.jboss.arquillian.warp.ServerAssertion;

@SuppressWarnings("unused")
public class SharingClass implements Serializable {

    public StaticInnerClass getStaticInnerClass() {

        ClientInterface client = new ClientImplementation();

        return new StaticInnerClass();
    }
    
    public InnerClass getInnerClass() {

        ClientInterface client = new ClientImplementation();

        return new InnerClass();
    }
    
    public ServerAssertion getAnonymousClass() {

        ClientInterface client = new ClientImplementation();

        return new ServerAssertion() {
            public void server() {
                ServerInterface server = new ServerImplemenation();
            }
        };
    }
    
    public class InnerClass extends ServerAssertion {
        
        public void server() {
            ServerInterface server = new ServerImplemenation();
        }
    }
    

    public static class StaticInnerClass extends ServerAssertion {

        public void server() {
            ServerInterface server = new ServerImplemenation();
        }
    }
}
