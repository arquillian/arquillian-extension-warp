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
package org.jboss.arquillian.warp.jsf.ftest.lifecycle;

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
/**
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * <p>
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * <p>
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

import static org.jboss.arquillian.warp.jsf.Phase.RENDER_RESPONSE;
import static org.junit.Assert.fail;

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
import org.jboss.arquillian.warp.exception.ServerWarpExecutionException;
import org.jboss.arquillian.warp.jsf.AfterPhase;
import org.jboss.arquillian.warp.jsf.ftest.cdi.CdiBean;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

@WarpTest
@RunAsClient
@RunWith(Arquillian.class)
public class TestFacesLifecycleFailurePropagation {

    /**
     * This test will fail in the profiles "tomee-managed" and "tomee-remote" with this error:
     *
     * javax.servlet.ServletException : null [Proxied because : Original exception caused: class java.io.InvalidClassException: javax.servlet.ServletException;
     * local class incompatible: stream classdesc serialVersionUID = 1, local class serialVersionUID = 4221302886851315160]
     *
     * Reason (see https://github.com/arquillian/arquillian-extension-warp/pull/108#issuecomment-1475388798):
     * extension/jsf-ftest/pom.xml declares a dependency "org.jboss.spec.javax.servlet:jboss-servlet-api_3.0_spec", where
     * no serialVersionUID is defined on "ServletException".
     * The TomEE implementation of "ServletException" from org.apache.tomcat:tomcat-servlet-api defines a serialVersionUID = 1.
     * This causes the error.
     *
     * Workaround: replace this in extension/jsf-ftest/pom.xml
     *     <dependency>
     *       <groupId>org.jboss.spec.javax.servlet</groupId>
     *       <artifactId>jboss-servlet-api_3.0_spec</artifactId>
     *       <scope>provided</scope>
     *     </dependency>
     *
     * with this:
     *
     *     <dependency>
     *       <groupId>org.apache.tomcat</groupId>
     *       <artifactId>tomcat-servlet-api</artifactId>
     *       <scope>provided</scope>
     *       <version>...version of TomEE...</version>
     *     </dependency>
     */

    @Drone
    WebDriver browser;

    @ArquillianResource
    URL contextPath;

    @Deployment
    public static WebArchive createDeployment() {

        return ShrinkWrap.create(WebArchive.class, "jsf-test.war").addClasses(CdiBean.class)
            .addAsWebResource(new File("src/main/webapp/index.xhtml"))
            .addAsWebInfResource(new File("src/main/webapp/WEB-INF/beans.xml"))
            .addAsWebResource(new File("src/main/webapp/templates/template.xhtml"), "templates/template.xhtml")
            .addAsWebInfResource("failing-listener.faces-config.xml", "faces-config.xml")
            .addClass(FailingPhaseListener.class);
    }

    @Test(expected = ServerWarpExecutionException.class)
    public void test() {
        Warp
            .initiate(new Activity() {
                public void perform() {
                    browser.navigate().to(contextPath + "index.jsf");
                }
            })
            .inspect(new Inspection() {
                         private static final long serialVersionUID = 1L;

                         @AfterPhase(RENDER_RESPONSE)
                         public void initial_state_havent_changed_yet() {
                             fail("test should not reach rendering phase");
                         }
                     }
            );

        fail("warp test should fail");
    }
}
