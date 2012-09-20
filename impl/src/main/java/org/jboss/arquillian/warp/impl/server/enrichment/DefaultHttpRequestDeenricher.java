package org.jboss.arquillian.warp.impl.server.enrichment;

import javax.servlet.http.HttpServletRequest;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.warp.impl.shared.RequestPayload;
import org.jboss.arquillian.warp.impl.utils.SerializationUtils;
import org.jboss.arquillian.warp.spi.WarpCommons;

public class DefaultHttpRequestDeenricher implements HttpRequestDeenricher {

    @Inject
    private Instance<HttpServletRequest> request;

    private String getStringPayload() {
        return request.get().getHeader(WarpCommons.ENRICHMENT_REQUEST);
    }

    @Override
    public boolean isEnriched() {
        return getStringPayload() != null;
    }

    @Override
    public RequestPayload resolvePayload() {
        String payload = getStringPayload();
        return SerializationUtils.deserializeFromBase64(payload);
    }

}
