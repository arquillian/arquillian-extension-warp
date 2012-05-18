Arquillian Warp
===============

This extension allows you to write client-side test which asserts server-side logic.

Warp has own extensions which allows you to integrate with various frameworks:

* Servlet (part of default implementation)
* Phaser (JSF lifecycle)


Configuration
-------------

Just add impl module to classpath and run test either from IDE or maven.

    <dependency>
        <groupId>org.jboss.arquillian.extension</groupId>
        <artifactId>arquillian-warp-impl</artifactId>
        <version>1.0.0.Alpha1-SNAPSHOT</version>
    </dependency>

or any framework-specific extension:

    <dependency>
        <groupId>org.jboss.arquillian.extension</groupId>
        <artifactId>arquillian-warp-phaser</artifactId>
        <version>1.0.0.Alpha1-SNAPSHOT</version>
    </dependency>

Use the servlet protocol in `arquillian.xml` configuration:

    <defaultProtocol type="Servlet 3.0"/>


Writing Warp tests
------------------

To allow your test to use Warp, place `@WarpTest` annotation to test class:

    @RunWith(Arquillian.class)
    @WarpTest
    public class PhaserBasicTest {
    }



Using `Warp` to trigger the client action
-----------------------------------------

You can use any HTTP client, such as @Drone WebDriver, to trigger the server logic:

    @Drone
    WebDriver browser;

Don't forget to force your test to run as client with `@RunAsClient` annotation.

Then use `Warp` utility class to run `execute` method.

    @Test
    @RunAsClient
    public void test() {
        Warp.execute(new ClientAction() {

            @Override
            public void action() {
                browser.navigate().to(contextPath + "index.jsf");
            }
        }).verify(new InitialRequestVerification());
    }

You need to provide `ClientAction` - the contract of this interface is that its `action` method leads to triggering HTTP request against `contextPath` URL.

Finally, in the `verify` method, you need to provide object which implements `ServerAssertion` interface.



Asserting server state with `ServerAssertion`
---------------------------------------------

In the `InitialRequestVerification` class, you provide test methods annotated with some of the lifecycle test annotations:

* `@BeforeServlet`
* `@AfterServlet`
* `@BeforePhase`
* `@AfterPhase`

Simple assertion may look like:

    public static class InitialRequestVerification implements ServerAssertion {

        @Inject
        CDIBean cdiBean;

        @AfterPhase(RENDER_RESPONSE)
        public void test_initial_state() {
            assertEquals("John", cdiBean.getName());
        }
    }

Note that you can use dependency injection to bring the classes such as CDI beans, EJB beans, or any other resource supported by Arquillian.
