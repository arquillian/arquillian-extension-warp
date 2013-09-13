Arquillian Warp [![Build Status](https://buildhive.cloudbees.com/job/arquillian/job/arquillian-extension-warp/badge/icon)](https://buildhive.cloudbees.com/job/arquillian/job/arquillian-extension-warp/)
===============

> Warp fills the void between client-side and server-side testing

This extension allows you to write client-side test which asserts server-side logic.

Warp has built-in support for following frameworks

* Servlet API
* JSF 2

and it has also several framework extensions

* [Spring MVC](http://arquillian.org/blog/2012/07/24/arquillian-extension-spring-1-0-0-Alpha2/) `Alpha` - led by [Jakub Narloch](https://github.com/jmnarloch/)
* [REST](https://github.com/arquillian/arquillian-extension-rest/tree/master/warp-rest) `Alpha` - led by [Jakub Narloch](https://github.com/jmnarloch/)
* [SeamTest migration layer](https://github.com/maschmid/warped-seam-test) `Proof of Concept` - led by [Marek Schmidt](https://github.com/maschmid)

Documentation
-------------

* [Draft for the Documentation](https://github.com/lfryc/arquillian.github.com/blob/warp-docs/docs/warp.adoc)

Reading
-------

* [Testing JSF with Arquillian Warp and Graphene](http://lukas.fryc.eu/blog/tags/testing-jsf/)
* [Warp Spring Extension](http://arquillian.org/blog/2012/07/24/arquillian-extension-spring-1-0-0-Alpha2/)
* [Arquillian Warp and TomEE](http://rmannibucau.wordpress.com/2012/10/23/arquillian-warp-and-tomee/)

<h3>Release blogs</h3>

* [1.0.0.Alpha4 Release Blog](http://arquillian.org/blog/2013/09/12/arquillian-extension-warp-1-0-0-Alpha4/)
* [1.0.0.Alpha3 Release Blog](http://arquillian.org/blog/2013/06/20/arquillian-extension-warp-1-0-0-Alpha3/)
* [1.0.0.Alpha2 Release Blog](http://arquillian.org/blog/2013/01/15/arquillian-extension-warp-1-0-0-Alpha2/)
* [1.0.0.Alpha1 Release Blog](http://arquillian.org/blog/2012/05/27/arquillian-extension-warp-1-0-0-Alpha1/)

Links
-----

* [Issue Tracker](https://issues.jboss.org/browse/ARQ/component/12315782)
* [Continuous Integration](https://arquillian.ci.cloudbees.com/job/Arquillian-Extension-Warp/)

Community
---------

* Chat: #arquillian channel @ [irc.freenode.net](http://webchat.freenode.net/)
* [Blogs](http://arquillian.org/blog/tags/warp/)
* [Forums](https://community.jboss.org/en/arquillian/dev)
* [Roadmap](https://community.jboss.org/thread/222044)

Getting Started
---------------

<h3>Setting up a project</h3>

Just add impl module to classpath and run test either from IDE or maven.

    <dependency>
        <groupId>org.jboss.arquillian.extension</groupId>
        <artifactId>arquillian-warp</artifactId>
        <version>1.0.0.Alpha4</version>
        <type>pom</type>
    </dependency>

or any framework-specific extension:

    <dependency>
        <groupId>org.jboss.arquillian.extension</groupId>
        <artifactId>arquillian-warp-jsf</artifactId>
        <version>1.0.0.Alpha4</version>
    </dependency>

Use the servlet protocol in `arquillian.xml` configuration:

    <defaultProtocol type="Servlet 3.0"/>

For more information on getting started, see documentation.

<h3>Writing Warp tests</h3>

To allow your test to use the Warp, place a `@WarpTest` annotation to the test class:

    @RunWith(Arquillian.class)
    @WarpTest
    @RunAsClient
    public class BasicTest {
    }

Don't forget to force Arquillian to run the test on a client with a `@RunAsClient` annotation.


<h3>Using `Warp` to trigger the client action</h3>

You can use any HTTP client, such as WebDriver (driven by [`@Drone`](https://docs.jboss.org/author/display/ARQ/Drone)), to trigger the server logic:

    @Drone
    WebDriver browser;

Then use `Warp` utility class to run `initiate` method.

    @Test
    public void test() {

        Warp
           .initiate(new Activity() {

                public void perform() {
                    browser.navigate().to(contextPath + "index.jsf");
                }})

           .inspect(new Inspection() {
                private static final long serialVersionUID = 1L;
            });
    }

You need to provide `Activity` - the contract of this interface is that its `perform` method leads to triggering one or more HTTP requests against `contextPath` URL (injected by Arquillian).

Finally, in the `inspect` method, you need to provide object which implements `Inspection` interface. This interface provides contract for object which can execute server-side logic.

Don't forget to provide `serialVersionUID` for `Inspection` objects.


<h3>Asserting server state with `Inspection`</h3>

In the `Inspection` implementation, you can provide test methods annotated with lifecycle-test annotations:

* `@BeforeServlet`
* `@AfterServlet`
* `@BeforePhase`
* `@AfterPhase`

Simple assertion may look like:

    new Inspection() {

        private static final long serialVersionUID = 1L;

        @Inject
        CDIBean cdiBean;

        @AfterPhase(RENDER_RESPONSE)
        public void test_initial_state() {
            assertEquals("John", cdiBean.getName());
        }
    }

Note that you can use dependency injection to bring the classes such as CDI beans, EJB beans, or any other resource supported by Arquillian.

Learning from Tests
-------------------

In order to explore more use cases for Warp, the best way is to explore functional tests:

* [Servlet API](https://github.com/arquillian/arquillian-extension-warp/tree/master/ftest/src/test/java/org/jboss/arquillian/warp/ftest)
* [JSF 2](https://github.com/arquillian/arquillian-extension-warp/tree/master/extension/jsf-ftest/src/test/java/org/jboss/arquillian/warp/jsf/ftest)
