package org.jboss.arquillian.warp.impl.client.event;

public class AdvertiseEnrichment {

    private int numberOfRequests;

    public AdvertiseEnrichment(int numberOfRequests) {
        this.numberOfRequests = numberOfRequests;
    }

    public int getNumberOfRequests() {
        return numberOfRequests;
    }

}
