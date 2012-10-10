package org.jboss.arquillian.warp.impl.shared.assertion;

import java.io.Serializable;

import org.jboss.arquillian.warp.ServerAssertion;
import org.jboss.arquillian.warp.impl.shared.RequestPayload;

@SuppressWarnings("unused")
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

        return new RequestPayload(new ServerAssertion() {
            public void server() {
                ServerInterface server = new ServerImplemenation();
            }
        });
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
