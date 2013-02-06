Arquillian Warp
===============

This extension allows you to write client-side test which asserts server-side logic.

Warp has built-in support for following frameworks

* Servlet API
* JSF 2

and it has also several framework extensions

* [Spring MVC](http://arquillian.org/blog/2012/07/24/arquillian-extension-spring-1-0-0-Alpha2/) `Alpha`
* [REST](https://github.com/jmnarloch/arquillian-extension-warp-rest/blob/master/ftest/ftest-resteasy/src/test/java/org/jboss/arquillian/quickstart/resteasy/service/rs/StockServiceAjaxTestCase.java#L78) `Proof of Concept`


Links
-----

* [Issue Tracker](https://issues.jboss.org/browse/ARQ/component/12315782)
* [Continuous Integration](https://arquillian.ci.cloudbees.com/job/Arquillian-Extension-Warp/)

Reading
-------

* [1.0.0.Alpha1 Release Blog](http://arquillian.org/blog/2012/05/27/arquillian-extension-warp-1-0-0-Alpha1/)
* [Warp Spring Extension](http://arquillian.org/blog/2012/07/24/arquillian-extension-spring-1-0-0-Alpha2/)

Community
---------

* IRC: #jbosstesting @ irc.freenode.net
* [Blogs](http://arquillian.org/blog/tags/warp/)
* [Forums](https://community.jboss.org/en/arquillian/dev)

Configuration
-------------

Just add impl module to classpath and run test either from IDE or maven.

    <dependency>
        <groupId>org.jboss.arquillian.extension</groupId>
        <artifactId>arquillian-warp</artifactId>
        <version>1.0.0.Alpha2</version>
        <type>pom</type>
    </dependency>

or any framework-specific extension:

    <dependency>
        <groupId>org.jboss.arquillian.extension</groupId>
        <artifactId>arquillian-warp-jsf</artifactId>
        <version>1.0.0.Alpha2</version>
    </dependency>

Use the servlet protocol in `arquillian.xml` configuration:

    <defaultProtocol type="Servlet 3.0"/>


Writing Warp tests
------------------

To allow your test to use the Warp, place a `@WarpTest` annotation to the test class:

    @RunWith(Arquillian.class)
    @WarpTest
    @RunAsClient
    public class BasicTest {
    }

Don't forget to force Arquillian to run the test on a client with a `@RunAsClient` annotation.


Using `Warp` to trigger the client action
------------------------------------------

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


Asserting server state with `Inspection`
-----------------------------------------

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

Learning
--------

In order to explore more use cases for Warp, the best way is to explore functional tests:

* [Servlet API](https://github.com/arquillian/arquillian-extension-warp/tree/master/ftest/src/test/java/org/jboss/arquillian/warp/ftest)
* [JSF 2](https://github.com/arquillian/arquillian-extension-warp/tree/master/extension/jsf-ftest/src/test/java/org/jboss/arquillian/warp/jsf/ftest)
