package xyz.staffjoy.faraday.assertions

import com.github.tomakehurst.wiremock.client.VerificationException
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder
import com.github.tomakehurst.wiremock.matching.UrlPattern
import org.springframework.http.HttpMethod

import static com.github.tomakehurst.wiremock.client.WireMock.absent
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import static com.github.tomakehurst.wiremock.client.WireMock.headRequestedFor
import static com.github.tomakehurst.wiremock.client.WireMock.optionsRequestedFor
import static com.github.tomakehurst.wiremock.client.WireMock.patchRequestedFor
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor
import static com.github.tomakehurst.wiremock.client.WireMock.traceRequestedFor
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.allRequests
import static org.apache.commons.lang3.StringUtils.EMPTY
import static org.springframework.http.HttpMethod.DELETE
import static org.springframework.http.HttpMethod.GET
import static org.springframework.http.HttpMethod.HEAD
import static org.springframework.http.HttpMethod.OPTIONS
import static org.springframework.http.HttpMethod.PATCH
import static org.springframework.http.HttpMethod.POST
import static org.springframework.http.HttpMethod.PUT
import static org.springframework.http.HttpMethod.TRACE

class ProxiedRequestAssert {

    private List<WireMockRule> actual

    protected ProxiedRequestAssert(WireMockRule... actual) {
        this.actual = actual
    }

    ProxiedRequestAssert withMethodAndUrl(HttpMethod method, String uri) {
        verify {
            it.verify(requestedFor(method, urlEqualTo(uri)))
        }
        return this
    }

    ProxiedRequestAssert withHeaders(Map<String, String> headers) {
        verify {
            def matcher = allRequests()
            headers.each { name, value ->
                value.split(', ').each {
                    matcher = matcher.withHeader(name, equalTo(it))
                }
            }
            it.verify(matcher)
        }
        return this
    }

    ProxiedRequestAssert withHeadersPlusPort(Map<String, String> headers, int port) {
        verify {
            def matcher = allRequests()
            headers.each { name, value ->
                value.split(', ').each {
                    matcher = matcher.withHeader(name, equalTo(it + ':' + port))
                }
            }
            it.verify(matcher)
        }
        return this
    }

    ProxiedRequestAssert withoutHeaders(List<String> headers) {
        verify {
            def matcher = allRequests()
            headers.each { name ->
                matcher = matcher.withHeader(name, absent())
            }
            it.verify(matcher)
        }
        return this
    }

    ProxiedRequestAssert withBody(String body) {
        verify {
            it.verify(allRequests().withRequestBody(equalTo(body)))
        }
        return this
    }

    ProxiedRequestAssert withoutBody() {
        verify {
            it.verify(allRequests().withRequestBody(equalTo(EMPTY)))
        }
        return this
    }

    private void verify(Closure verification) {
        def error = EMPTY
        def matchesCount = actual.count {
            try {
                verification(it)
                return true
            } catch (VerificationException ex) {
                error += "$ex.message\n\n"
                return false
            }
        }
        if (matchesCount != 1) {
            throw new VerificationException(error)
        }
    }

    private static RequestPatternBuilder requestedFor(HttpMethod method, UrlPattern urlPattern) {
        switch (method) {
            case DELETE:
                return deleteRequestedFor(urlPattern)
            case POST:
                return postRequestedFor(urlPattern)
            case GET:
                return getRequestedFor(urlPattern)
            case PUT:
                return putRequestedFor(urlPattern)
            case OPTIONS:
                return optionsRequestedFor(urlPattern)
            case TRACE:
                return traceRequestedFor(urlPattern)
            case HEAD:
                return headRequestedFor(urlPattern)
            case PATCH:
                return patchRequestedFor(urlPattern)
            default:
                throw new RuntimeException("Invalid HTTP method: $method")
        }
    }
}
