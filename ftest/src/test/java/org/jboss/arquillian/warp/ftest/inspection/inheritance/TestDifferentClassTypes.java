/**
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
package org.jboss.arquillian.warp.ftest.inspection.inheritance;

import java.io.File;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.warp.Activity;
import org.jboss.arquillian.warp.Inspection;
import org.jboss.arquillian.warp.Warp;
import org.jboss.arquillian.warp.WarpTest;
import org.jboss.arquillian.warp.ftest.TestingServlet;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

@RunWith(Arquillian.class)
@WarpTest
@RunAsClient
public class TestDifferentClassTypes {

    @Drone
    WebDriver browser;

    @ArquillianResource
    URL contextPath;

    @Deployment
    public static WebArchive createDeployment() {

        return ShrinkWrap.create(WebArchive.class, "test.war")
                .addClasses(TestingServlet.class, AbstractInspection.class, InspectionWithConstructorWithArguments.class, InspectionExtendingInspectionWithConstructorWithArguments.class)
                .addAsWebResource(new File("src/main/webapp/index.html"))
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void test_static_inner_class() {
        Warp
            .initiate(new Activity() {
                public void perform() {
                    browser.navigate().to(contextPath + "index.html");
                }})
            .inspect(new StaticInnerClass());
    }

    @Test
    public void test_static_inner_class_with_constructor_with_arguments() {
        Warp
            .initiate(new Activity() {
                public void perform() {
                    browser.navigate().to(contextPath + "index.html");
                }})
            .inspect(new StaticInnerClassWithConstructorWithArguments(null));
    }

    @Test
    @Ignore("ARQ-1253")
    public void test_static_class_extending_class_with_constructor_with_arguments() {
        Warp
            .initiate(new Activity() {
                public void perform() {
                    browser.navigate().to(contextPath + "index.html");
                }})
            .inspect(new StaticInnerClassExtendingClassWithConstructorWithArguments(null));
    }

    @Test
    public void test_class_with_constructor_with_arguments() {
        Warp
            .initiate(new Activity() {
                public void perform() {
                    browser.navigate().to(contextPath + "index.html");
                }})
            .inspect(new InspectionWithConstructorWithArguments(null));
    }

    @Test
    public void test_class_extending_class_with_constructor_with_arguments() {
        Warp
            .initiate(new Activity() {
                public void perform() {
                    browser.navigate().to(contextPath + "index.html");
                }})
            .inspect(new InspectionExtendingInspectionWithConstructorWithArguments(null));
    }

    public static class StaticInnerClass extends Inspection {
        private static final long serialVersionUID = 1L;
    }

    public static class StaticInnerClassWithConstructorWithArguments extends Inspection {
        private static final long serialVersionUID = 1L;

        public StaticInnerClassWithConstructorWithArguments(Object argument) {
        }
    }

    public static class StaticInnerClassExtendingClassWithConstructorWithArguments extends
            InspectionWithConstructorWithArguments {
        private static final long serialVersionUID = 1L;

        public StaticInnerClassExtendingClassWithConstructorWithArguments(Object argument) {
            super(argument);
        }
    }
}
