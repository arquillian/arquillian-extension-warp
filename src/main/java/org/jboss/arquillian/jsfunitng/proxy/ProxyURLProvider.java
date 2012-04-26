package org.jboss.arquillian.jsfunitng.proxy;

import java.lang.annotation.Annotation;
import java.net.URL;

import org.jboss.arquillian.container.test.impl.enricher.resource.URLResourceProvider;
import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.graphene.utils.URLUtils;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;

// TODO needs to have priority over URLResourceProvider
public class ProxyURLProvider implements ResourceProvider {

    @Inject
    Instance<ServiceLoader> serviceLoader;

    @Inject
    Instance<ProxyDetails> proxyDetails;

    @Inject
    Instance<ProxyHolder> proxyHolder;

    @Inject
    Instance<Injector> injector;

    URLResourceProvider urlResourceProvider = new URLResourceProvider();

    @Override
    public boolean canProvide(Class<?> type) {
        return URL.class.isAssignableFrom(type);
    }

    @Override
    public Object lookup(ArquillianResource resource, Annotation... qualifiers) {
        injector.get().inject(urlResourceProvider);

        URL realUrl = (URL) urlResourceProvider.lookup(resource, qualifiers);
        if ("http".equals(realUrl.getProtocol())) {
            return getProxyUrl(realUrl);
        } else {
            return realUrl;
        }
    }

    private URL getProxyUrl(URL realUrl) {
        URL baseRealUrl = URLUtils.getUrlBase(realUrl);
        URL baseProxyUrl = proxyDetails.get().getProxyURL(baseRealUrl);
        URL proxyUrl = URLUtils.buildUrl(baseProxyUrl, realUrl.getPath());

        proxyHolder.get().startProxyForUrl(baseProxyUrl, baseRealUrl);

        return proxyUrl;
    }

}
