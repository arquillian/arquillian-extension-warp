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
package org.jboss.arquillian.warp.impl.client.context.operation;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

/**
 * {@link Contextualizer} is able to wrap operation so that it can be run in another thread with given contexts activated.
 *
 * @author Lukas Fryc
 */
public class Contextualizer {

    /**
     * Contextualizes operation with contexts given by {@link OperationalContext}
     *
     * @param context                      the context
     * @param instance                     the instance to wrap (must comply with given interface)
     * @param interfaze                    the interface of return object
     * @param contextPropagatingInterfaces when a return type of any invocation is one of these interfaces, the given result will be call contextually as well
     */
    public static <T> T contextualize(final OperationalContext context, final T instance, Class<?> interfaze,
        Class<?>... contextPropagatingInterfaces) {

        OperationalContextRetriever retriever = new OperationalContextRetriever() {
            @Override
            public OperationalContext retrieve() {
                return context;
            }
        };

        return contextualize(retriever, instance, interfaze);
    }

    /**
     * Contextualizes operation with contexts given by {@link OperationalContext} which is given by provided
     * {@link OperationalContextRetriever}
     *
     * @param retriever                    the context retriever
     * @param instance                     the instance to wrap (must comply with given interface)
     * @param interfaze                    the interface of return object
     * @param contextPropagatingInterfaces when a return type of any invocation is one of these interfaces, the given result will be call contextually as well
     */
    @SuppressWarnings("unchecked")
    public static <T> T contextualize(final OperationalContextRetriever retriever, final T instance, Class<?> interfaze,
                                      final Class<?>... contextPropagatingInterfaces) {
        return (T) Proxy.newProxyInstance(instance.getClass().getClassLoader(), new Class<?>[] {interfaze},
            new InvocationHandler() {
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    OperationalContext context = retriever.retrieve();
                    context.activate();
                    try {
                        Object result = method.invoke(instance, args);
                        Class<?> type = method.getReturnType();
                        if (result != null && type != null && type.isInterface() && Arrays.asList(
                            contextPropagatingInterfaces).contains(type)) {
                            return contextualize(retriever, result, type, contextPropagatingInterfaces);
                        } else {
                            return result;
                        }
                    } catch (InvocationTargetException e) {
                        throw e.getTargetException();
                    } finally {
                        context.deactivate();
                    }
                }
            });
    }
}
