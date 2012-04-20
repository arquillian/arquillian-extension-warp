package org.jboss.arquillian.jsfunitng.javascript;
import org.jboss.arquillian.graphene.javascript.Dependency;
import org.jboss.arquillian.graphene.javascript.InstallableJavaScript;
import org.jboss.arquillian.graphene.javascript.JavaScript;
import org.jboss.arquillian.graphene.page.interception.XhrInjection;

@JavaScript("JSFUnit.requestEnrichment")
@Dependency(sources = "jsfunit.js", interfaces = XhrInjection.class)
public interface RequestEnrichment extends InstallableJavaScript {

    void setRequestEnrichment(String enrichment);

    String getResponseEnrichment();

    void clearEnrichment();
}
