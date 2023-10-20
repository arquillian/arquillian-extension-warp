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
package org.jboss.arquillian.warp.ftest.failure;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.NotSerializableException;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.warp.Activity;
import org.jboss.arquillian.warp.Inspection;
import org.jboss.arquillian.warp.Warp;
import org.jboss.arquillian.warp.WarpTest;
import org.jboss.arquillian.warp.exception.ClientWarpExecutionException;
import org.jboss.arquillian.warp.exception.ServerWarpExecutionException;
import org.jboss.arquillian.warp.ftest.TestingServlet;
import org.jboss.arquillian.warp.impl.utils.URLUtils;
import org.jboss.arquillian.warp.servlet.BeforeServlet;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Lukas Fryc
 */
@RunWith(Arquillian.class)
@WarpTest
@RunAsClient
public class TestSerializationFailurePropagation {

    @ArquillianResource
    URL contextPath;

    @Deployment
    public static WebArchive createDeployment() {

        return ShrinkWrap
            .create(WebArchive.class, "test.war")
            .addClass(TestingServlet.class)
            .addAsWebResource(new File("src/main/webapp/index.html"))
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void when_serialization_of_inspection_fails_on_client_then_client_failure_should_be_reported() {

        try {
            Warp
                .initiate(new Activity() {
                    public void perform() {
                        try {
                            URLUtils.buildUrl(contextPath, "index.html").getContent();
                        } catch (IOException e) {
                        }
                        fail("the enrichment exception should be reported");
                    }
                })
                .inspect(new Inspection() {
                             private static final long serialVersionUID = 1L;

                             @SuppressWarnings("unused")
                             private Object payload = new NonSerializableObject();

                             @BeforeServlet
                             public void beforeServlet() {
                                 fail("the inspection should never reach the server");
                             }
                         }
                );
            fail("the test should fail to serialize object");
        } catch (ClientWarpExecutionException e) {
            assertThat(e.getMessage(), containsString(NotSerializableException.class.getName()));
            assertThat(e.getMessage(), containsString(NonSerializableObject.class.getName()));
        }
    }

    @Test
    public void when_serialization_of_inspection_fails_on_server_then_server_failure_should_be_reported() {
        try {
            Warp
                .initiate(new Activity() {
                    public void perform() {
                        try {
                            URLUtils.buildUrl(contextPath, "index.html").getContent();
                        } catch (IOException e) {
                        }
                        fail("the enrichment exception should be reported");
                    }
                })
                .inspect(new Inspection() {
                             private static final long serialVersionUID = 1L;

                             @SuppressWarnings("unused")
                             private Object payload;

                             @BeforeServlet
                             public void beforeServlet() {
                                 payload = new NonSerializableObject();
                             }
                         }
                );
            fail("the test should fail to serialize object");
        } catch (ServerWarpExecutionException e) {
            assertThat(e.getMessage(), containsString(NotSerializableException.class.getName()));
            assertThat(e.getMessage(), containsString(NonSerializableObject.class.getName()));
        }
    }

    public static class NonSerializableObject {
    }
}
