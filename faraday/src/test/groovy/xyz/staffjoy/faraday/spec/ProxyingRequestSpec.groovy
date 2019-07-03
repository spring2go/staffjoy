package xyz.staffjoy.faraday.spec

import spock.lang.Unroll
import xyz.staffjoy.faraday.BasicSpec

import static xyz.staffjoy.faraday.assertions.Assertions.assertThat
import static org.springframework.http.HttpMethod.DELETE
import static org.springframework.http.HttpMethod.GET
import static org.springframework.http.HttpMethod.OPTIONS
import static org.springframework.http.HttpMethod.POST
import static org.springframework.http.HttpMethod.PUT
import static org.springframework.http.HttpMethod.TRACE
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR

class ProxyingRequestSpec extends BasicSpec {
    @Unroll
    def "Should proxy HTTP request preserving request method when method is #method"() {
        when:
        sendRequest method, 'www.staffjoy-v2.local', '/path/1'

        then:
        assertThat(localhost8086, localhost8087)
                  .haveReceivedRequest()
                  .withMethodAndUrl(method, '/path/1')
                  .withoutBody()

        where:
        method << [GET, POST, DELETE, PUT, TRACE]
    }

    @Unroll
    def "Should not proxy HTTP request preserving request method when method is OPTIONS"() {
        when:
        def response = sendRequest method, 'www.staffjoy-v2.local', '/path/1'

        then:
        assertThat(localhost8086, localhost8087)
                .haveReceivedNoRequest()

        where:
        method << [OPTIONS]
    }

    @Unroll
    def "Should proxy HTTP request to #destinationUrl when request uri is #requestUri"() {
        when:
        sendRequest GET, 'www.staffjoy-v2.local', requestUri

        then:
        assertThat(localhost8086, localhost8087)
                .haveReceivedRequest()
                .withMethodAndUrl(GET, destinationUri)
                .withoutBody()

        where:
        requestUri                       | destinationUri
        '/'                              | '/'
        '/?param1=1&param2'              | '/?param1=1&param2'
        '/?param1=1&param2'              | '/?param1=1&param2'
        '/path/1'                        | '/path/1'
        '/path/1?param1=1&param2'        | '/path/1?param1=1&param2'
    }

    @Unroll
    def "Should proxy HTTP request with headers #destinationHeaders when request headers are #requestHeaders including X-Forwarded headers"() {
        when:
        sendRequest GET, 'www.staffjoy-v2.local', '/path/1', requestHeaders

        then:
        assertThat(localhost8086, localhost8087)
                 .haveReceivedRequest()
        .withMethodAndUrl(GET, '/path/1')
        .withHeaders(destinationHeaders)
        .withHeaders(['X-Forwarded-Proto': 'http', 'X-Forwarded-Host': 'www.staffjoy-v2.local', 'X-Forwarded-Port': port.toString()])
        .withoutBody()

        where:
        requestHeaders                                                   | destinationHeaders
        [:]                                                              | ['X-Forwarded-For': '127.0.0.1']
        ['Header-1': 'Value 1']                                          | ['Header-1': 'Value 1', 'X-Forwarded-For': '127.0.0.1']
        ['Header-1': 'Value 1', 'Header-2': 'Value 2', 'Header-3': '']   | ['Header-1': 'Value 1', 'Header-2': 'Value 2', 'X-Forwarded-For': '127.0.0.1']
        ['X-Forwarded-For': '172.10.89.11']                              | ['X-Forwarded-For': '172.10.89.11, 127.0.0.1']
        ['X-Forwarded-For': '172.10.89.11']                              | ['X-Forwarded-For': '172.10.89.11, 127.0.0.1']
    }

    @Unroll
    def "Should proxy HTTP request with headers #destinationHeaders when request headers are #requestHeaders"() {

        when:
        sendRequest GET, 'ical.staffjoy-v2.local', '/path/9', requestHeaders

        then:
        assertThat(localhost8086)
                 .haveReceivedRequest()
                 .withMethodAndUrl(GET, '/path/9')
                 .withHeadersPlusPort(destinationHeaders, port)
                 .withoutHeaders(removedHeaders)
                 .withoutBody()

        where:
        requestHeaders                            | destinationHeaders         | removedHeaders
        [:]                                       | ['Host': 'ical.staffjoy-v2.local'] | []
        ['TE': 'compress']                        | ['Host': 'ical.staffjoy-v2.local'] | ['TE']
    }

    @Unroll
    def "Should proxy HTTP request preserving request body when body is '#body'"() {
        when:
        sendRequest POST, 'www.staffjoy-v2.local', '/path/1', [:], body

        then:
        assertThat(localhost8086, localhost8087)
                .haveReceivedRequest()
                .withMethodAndUrl(POST, "/path/1")
                .withBody(body)

        where:
        body << ['', '   ', 'Sample body']
    }

    @Unroll
    def "Should fail to proxy HTTP request when destination URL cannot be created"() {
        when:
        def response = sendRequest GET, "faraday.staffjoy-v2.local", "/path/4"

        then:
        assertThat(localhost8086, localhost8087)
                  .haveReceivedNoRequest()
        assertThat(response)
                 .hasStatus(INTERNAL_SERVER_ERROR)
    }
}
