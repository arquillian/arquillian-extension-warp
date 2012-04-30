package org.jboss.arquillian.warp.utils;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Provides URL manipulations and functionality.
 * 
 * @author Lukas Fryc
 */
public final class URLUtils {

    private URLUtils() {
    }

    /**
     * Use URL context and one or more relocations to build end URL.
     * 
     * @param context first URL used like a context root for all relocation changes
     * @param relocations array of relocation URLs
     * @return end url after all changes made on context with relocations
     * @throws AssertionError when context or some of relocations are malformed URLs
     */
    public static URL buildUrl(String context, String... relocations) {
        try {
            return buildUrl(new URL(context), relocations);
        } catch (MalformedURLException e) {
            throw new AssertionError("URL('" + context + "') isn't valid URL");
        }
    }

    /**
     * Use URL context and one or more relocations to build end URL.
     * 
     * @param context first URL used like a context root for all relocation changes
     * @param relocations array of relocation URLs
     * @return end url after all changes made on context with relocations
     * @throws AssertionError when context or some of relocations are malformed URLs
     */
    public static URL buildUrl(URL context, String... relocations) {
        URL url = context;

        for (String move : relocations) {
            try {
                url = new URL(url, move);
            } catch (MalformedURLException e) {
                throw new AssertionError("URL('" + url + "', '" + move + "') isn't valid URL");
            }
        }

        return url;
    }

    public static URL getUrlBase(URL url) {
        try {
            return new URL(url.getProtocol(), url.getHost(), url.getPort(), "");
        } catch (MalformedURLException e) {
            throw new AssertionError("URL('" + url + "') was unable to transform to base URL");
        }
    }

    public static URL changeBase(URL url, URL base) {
        base = URLUtils.getUrlBase(base);
        return URLUtils.buildUrl(base, url.getFile());
    }
}
