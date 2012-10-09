package org.jboss.arquillian.warp.server.assertion;

import org.jboss.arquillian.warp.ServerAssertion;
import org.jboss.arquillian.warp.client.execution.AssertionTransformer;
import org.jboss.arquillian.warp.extension.servlet.BeforeServlet;
import org.jboss.arquillian.warp.impl.shared.RequestPayload;
import org.jboss.arquillian.warp.impl.utils.SerializationUtils;
import org.jboss.arquillian.warp.testutils.SeparatedClassPath;
import org.jboss.arquillian.warp.testutils.SeparatedClassloader;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.resolver.api.DependencyResolvers;
import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SeparatedClassloader.class)
public class AssertionTransformerOnSeparatedClassLoaderTestCase {
    
    @SeparatedClassPath
    public static JavaArchive archive() {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class)
                .addClasses(ServerAssertion.class, RequestPayload.class, BeforeServlet.class)
                .addClasses(SerializationUtils.class)
                .addClasses(AssertionTransformer.class)
                .addClasses(AssertionTransformerTestCase.class);
        
        JavaArchive javassist = DependencyResolvers
            .use(MavenDependencyResolver.class)
            .artifact("javassist:javassist:3.12.1.GA")
            .resolveAs(JavaArchive.class)
            .iterator().next();
        
        JavaArchive junit = DependencyResolvers
                .use(MavenDependencyResolver.class)
                .artifact("junit:junit:4.10")
                .resolveAs(JavaArchive.class)
                .iterator().next();
        
        return archive.merge(javassist).merge(junit);
    }
    
    @Test
    public void testAnonymousClass() throws Exception {

        ServerAssertion assertion = AssertionTransformerTestCase.getAnonymousServerAssertion();

        byte[] classFile = AssertionTransformer.transform(assertion.getClass());
        Object modifiedAssertion = AssertionTransformer.cloneToNew(assertion, classFile);

        AssertionTransformerTestCase.verifyServerAssertionClass(modifiedAssertion);
    }
    
    @Test
    public void testSerialization() throws Exception {

        ServerAssertion assertion = AssertionTransformerTestCase.getAnonymousServerAssertion();

        RequestPayload payload = new RequestPayload(assertion);

        RequestPayload deserializedPayload = SerializationUtils.deserializeFromBytes(SerializationUtils
                .serializeToBytes(payload));
        ServerAssertion deserializedAssertion = deserializedPayload.getAssertion();

        AssertionTransformerTestCase.verifyServerAssertionClass(deserializedAssertion);
    }
}
