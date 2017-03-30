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
package org.jboss.arquillian.warp.impl.client.proxy;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URL;

import org.jboss.arquillian.warp.impl.client.context.operation.OperationalContext;
import org.jboss.arquillian.warp.impl.utils.URLUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TestProxyURLToContextMapping {

    private URL url = URLUtils.buildUrl("http://localhost:18080");
    private URL anotherUrl = URLUtils.buildUrl("http://localhost:18081");

    @Mock
    private OperationalContext context;

    private ProxyURLToContextMapping mapping;

    @Before
    public void setUp() {
        mapping = new ProxyURLToContextMapping();
    }

    @Test(expected = IllegalStateException.class)
    public void when_another_classes_for_same_url_are_registered_then_mapping_should_fail_to_register() {
        mapping.register(url, TestingClass.class, context);
        mapping.register(url, AnotherClass.class, context);
    }

    @Test
    public void when_more_urls_are_registered_to_one_class_then_mapping_should_carry_them_all() {
        mapping.register(url, TestingClass.class, context);
        mapping.register(anotherUrl, TestingClass.class, context);
    }

    @Test
    public void when_url_is_registered_then_mapping_should_be_able_to_indicate_that() {
        assertFalse(mapping.isRegistered(url));
        mapping.register(url, TestingClass.class, context);
        assertTrue(mapping.isRegistered(url));
    }

    private static class TestingClass {
    }

    private static class AnotherClass {
    }
}
