package xyz.staffjoy.faraday.assertions

import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder

class DestinationAssert {

    private WireMockRule[] actual

    protected DestinationAssert(WireMockRule... actual) {
        this.actual = actual
    }

    ProxiedRequestAssert haveReceivedRequest() {
        return new ProxiedRequestAssert(actual)
    }

    DestinationAssert haveReceivedNoRequest() {
        actual.each {
            it.verify(0, RequestPatternBuilder.allRequests())
        }
        return this
    }
}
