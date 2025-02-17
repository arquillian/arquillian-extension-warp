This project contains a few tests that were initially part of the "arquillian-warp-impl" module and used a 
JUnit4 `BlockJUnit4ClassRunner` (latest code can be found here: https://github.com/arquillian/arquillian-extension-warp/blob/c19404991c2599f5d47df0433097da19cb24968e/impl/src/test/java/org/jboss/arquillian/warp/impl/testutils/SeparatedClassloaderRunner.java)

As this feature is not supported by JUnit5, a new approach was created using the sample from https://github.com/junit-team/junit5/discussions/4203.

Nearly all tests in this project create a java archive, which is wrapped by a ShrinkWrapClassLoader.
The test methode shall be executed on this separate classloader
to simulate the test execution by arquillian, where the test jar is assembled on the client,
then deployed to the server, and the test is executed inside the server.

To run a tests with a separated classloader, the test class has to be annotated like this:
```
@ExtendWith(SeparatedClassloaderExtension.class)
public class MyTest {
}
```

And the test needs a method that is annotated with ```org.jboss.arquillian.warp.impl.testutils.SeparatedClassPath``` and returns an array of JavaArchives.
```
@SeparatedClassPath
public static JavaArchive[] archive() {
}
```

**Part one** of  the "separated classloader" workaround is found in ```org.jboss.arquillian.warp.impl.testutils.SeparatedClassloaderExtension```

This listener detects (method ```SeparatedClassloaderLauncherSessionListener#launcherSessionOpened```)
whether a test has a method annotated with ```@SeparatedClassPath```.
If one is found, it creates a ShrinkWrapClassLoader, which uses the JavaArchives returned by this method. Then it reloads
the test class from this class loader.

The ShrinkWrapClassLoader is created with a parent classloader that is called "filtering classloader" here, whose purpose is
(quote from the JUnit question): *to pretend the test class wasn't yet loaded so the child class loader will load it again (after asking its parent).*

This "filtering classloader" is necessary, otherwise the test classes will not be loaded from the ShrinkwrapClassLoader.
If the test method uses any other classes that are bundled in jars in the ShrinkWrapClassLoader, the filtering classloader has
to filter those classes, too, so they have to be added here.

**Part two** of  the "separated classloader" workaround is found in ```org.jboss.arquillian.warp.impl.testutils.SeparatedClassloaderLauncherSessionListener```.

This extension switches the current thread context classloader to the classloader of the test class, which is the ShrinkWrapClassLoader
of the test class.
