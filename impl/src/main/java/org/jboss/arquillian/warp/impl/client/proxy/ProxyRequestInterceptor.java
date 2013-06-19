package org.jboss.arquillian.warp.impl.client.proxy;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Future;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.littleshoot.proxy.ProxyCacheManager;
import org.littleshoot.proxy.ProxyUtils;

public class ProxyRequestInterceptor implements ProxyCacheManager {

    @Override
    public boolean returnCacheHit(HttpRequest request, Channel channel) {
        System.out.println(request);
        String uri = request.getUri();
        String host;
        try {
            host = new URI(uri).getHost();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
//        if (host.equals("localhost")) {
//            rejectRequest(channel);
//            ProxyUtils.closeOnFlush(channel);
//            return true;
//        }
        return false;
    }

    @Override
    public Future<String> cache(HttpRequest originalRequest, HttpResponse httpResponse, Object response, ChannelBuffer encoded) {
        return null;
    }

    private void rejectRequest(Channel channel) {
        final String statusLine = "HTTP/1.1 407 Proxy Authentication Required\r\n";
        final String headers = "Date: " + ProxyUtils.httpDate() + "\r\n"
                + "Proxy-Authenticate: Basic realm=\"Restricted Files\"\r\n" + "Content-Length: 415\r\n"
                + "Content-Type: text/html; charset=iso-8859-1\r\n" + "\r\n";

        final String responseBody = "<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">\n" + "<html><head>\n"
                + "<title>407 Proxy Authentication Required</title>\n" + "</head><body>\n"
                + "<h1>Proxy Authentication Required</h1>\n" + "<p>This server could not verify that you\n"
                + "are authorized to access the document\n" + "requested.  Either you supplied the wrong\n"
                + "credentials (e.g., bad password), or your\n" + "browser doesn't understand how to supply\n"
                + "the credentials required.</p>\n" + "</body></html>\n";
        System.out.println("Content-Length is really: " + responseBody.length());
        ProxyUtils.writeResponse(channel, statusLine, headers, responseBody);
    }

}