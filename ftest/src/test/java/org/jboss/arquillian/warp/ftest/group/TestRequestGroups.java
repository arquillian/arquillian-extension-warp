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
package org.jboss.arquillian.warp.ftest.group;

import static org.jboss.arquillian.warp.client.filter.http.HttpFilters.request;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.warp.Activity;
import org.jboss.arquillian.warp.Inspection;
import org.jboss.arquillian.warp.Warp;
import org.jboss.arquillian.warp.WarpTest;
import org.jboss.arquillian.warp.client.exception.MultipleGroupsPerRequestException;
import org.jboss.arquillian.warp.client.result.WarpGroupResult;
import org.jboss.arquillian.warp.client.result.WarpResult;
import org.jboss.arquillian.warp.ftest.TestingServlet;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.WebDriver;

/**
 * @author Lukas Fryc
 */
@ExtendWith(ArquillianExtension.class)
@WarpTest
@RunAsClient
public class TestRequestGroups {

    @Drone
    WebDriver browser;

    @ArquillianResource
    URL contextPath;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "test.war")
            .addClass(TestingServlet.class)
            .addAsWebResource(new File("src/main/webapp/index.html"))
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void when_there_are_two_matching_groups_per_request_then_exception_should_be_thrown() {
        Assertions.assertThrows(MultipleGroupsPerRequestException.class, () -> {
            Warp
                .initiate(new Activity() {
                    public void perform() {
                        browser.navigate().to(contextPath + "index.html");
                    }
                })
                .group(1)
                .inspect(new Inspection() {
                    private static final long serialVersionUID = 1L;
                })
                .group(2)
                .inspect(new Inspection() {
                    private static final long serialVersionUID = 1L;
                })
                .execute();
        });
    }

    @Test
    public void test_two_requests() {
        WarpResult result = Warp
            .initiate(new Activity() {
                public void perform() {
                    browser.navigate().to(contextPath + "index.html");
                    browser.navigate().to(contextPath + "index.html");
                }
            })
            .group(1)
            .observe(request().uri().endsWith("index.html").index(1))
            .inspect(new Inspection() {
                private static final long serialVersionUID = 1L;
            })
            .group(2)
            .observe(request().uri().endsWith("index.html").index(2))
            .inspect(new Inspection() {
                private static final long serialVersionUID = 1L;
            })
            .execute();

        WarpGroupResult group = result.getGroup(1);
        assertEquals(1, group.getHitCount(), "first group get hit once");
    }
}
