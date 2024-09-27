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
package org.jboss.arquillian.warp.impl.client.filter.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.jboss.arquillian.warp.client.filter.http.HttpFilters.request;

import org.jboss.arquillian.warp.client.execution.WarpActivityBuilder;
import org.jboss.arquillian.warp.client.execution.WarpRuntime;
import org.jboss.arquillian.warp.client.filter.http.HttpFilterBuilder;
import org.jboss.arquillian.warp.client.filter.http.HttpMethod;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestMatchersToString {

    @Test
    public void testContainsHeader() {
        assertThat(request().header().containsHeader("xyz").toString(), equalTo("containsHeader('xyz')"));
        assertThat(request().header().not().containsHeader("xyz").toString(), equalTo("not containsHeader('xyz')"));
    }

    @Test
    public void testContainsHeaderValue() {
        assertThat(request().header().containsValue("xyz", "abc").toString(), equalTo("containsValue('xyz', 'abc')"));
        assertThat(request().header().not().containsValue("xyz", "abc").toString(),
            equalTo("not containsValue('xyz', 'abc')"));
    }

    @Test
    public void testHeaderEqual() {
        assertThat(request().header().equal("xyz", "abc").toString(), equalTo("header.equal('xyz', 'abc')"));
        assertThat(request().header().not().equal("xyz", "abc").toString(), equalTo("not header.equal('xyz', 'abc')"));
    }

    @Test
    public void testIndex() {
        assertThat(request().index(1).toString(), equalTo("index(1)"));
    }

    @Test
    public void testMethod() {
        assertThat(request().method().equal(HttpMethod.GET).toString(), equalTo("method(GET)"));
        assertThat(request().method().not().equal(HttpMethod.POST).toString(), equalTo("not method(POST)"));
    }

    @Test
    public void testUriContains() {
        assertThat(request().uri().contains("xyz").toString(), equalTo("uri.contains('xyz')"));
        assertThat(request().uri().not().contains("xyz").toString(), equalTo("not uri.contains('xyz')"));
    }

    @Test
    public void testUriEndsWith() {
        assertThat(request().uri().endsWith("xyz").toString(), equalTo("uri.endsWith('xyz')"));
        assertThat(request().uri().not().endsWith("xyz").toString(), equalTo("not uri.endsWith('xyz')"));
    }

    @Test
    public void testUriEqual() {
        assertThat(request().uri().equal("xyz").toString(), equalTo("uri.equal('xyz')"));
        assertThat(request().uri().not().equal("xyz").toString(), equalTo("not uri.equal('xyz')"));
    }

    @Test
    public void testUriEqualIgnoreCase() {
        assertThat(request().uri().equalIgnoreCase("xyz").toString(), equalTo("uri.equalIgnoreCase('xyz')"));
        assertThat(request().uri().not().equalIgnoreCase("xyz").toString(), equalTo("not uri.equalIgnoreCase('xyz')"));
    }

    @Test
    public void testUriMatches() {
        assertThat(request().uri().matches("xyz").toString(), equalTo("uri.matches('xyz')"));
        assertThat(request().uri().not().matches("xyz").toString(), equalTo("not uri.matches('xyz')"));
    }

    @Test
    public void testUriStartsWith() {
        assertThat(request().uri().startsWith("xyz").toString(), equalTo("uri.startsWith('xyz')"));
        assertThat(request().uri().not().startsWith("xyz").toString(), equalTo("not uri.startsWith('xyz')"));
    }

    @Before
    public void beforeTest() {
        WarpRuntime.setInstance(new WarpRuntime() {

            @Override
            public WarpActivityBuilder getWarpActivityBuilder() {
                throw new UnsupportedOperationException();
            }

            @Override
            public HttpFilterBuilder getHttpFilterBuilder() {
                return new DefaultHttpFilterBuilder();
            }
        });
    }

    @After
    public void afterTest() {
        WarpRuntime.setInstance(null);
    }
}
