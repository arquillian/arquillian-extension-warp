package org.jboss.arquillian.warp.impl.client.operation;


public class Wrapper {

    public static <A, T, X extends Operation<A, T>> X wrap(final OperationalContext context, final X operation) {

        OperationalContextRetriver retriever = new OperationalContextRetriver() {
            @Override
            public OperationalContext retrieve() {
                return context;
            }
        };

        return wrap(retriever, operation);
    }

    @SuppressWarnings("unchecked")
    public static <A, T, X extends Operation<A, T>> X wrap(final OperationalContextRetriver retriver, final X operation) {

        return (X) new Operation<A, T>() {
            @Override
            public T perform(A argument) {
                OperationalContext context = retriver.retrieve();
                context.activate();
                try {
                    return operation.perform(argument);
                } finally {
                    context.deactivate();
                }
            }
        };
    }
}
