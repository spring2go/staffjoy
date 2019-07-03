package xyz.staffjoy.faraday.spec

import static xyz.staffjoy.faraday.assertions.Assertions.assertThat
import spock.lang.Unroll
import xyz.staffjoy.faraday.BasicSpec

import static org.springframework.http.HttpMethod.GET
import static org.springframework.http.HttpStatus.BAD_REQUEST
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import static org.springframework.http.HttpStatus.OK
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.FOUND
import static org.springframework.http.HttpStatus.NOT_FOUND
class ProxyingResponseSpec extends BasicSpec {

    @Unroll
    def "Should get proxied HTTP response with preserved status when destination response status is success #status"() {

        given:
        stubDestinationResponse status

        when:
        def response = sendRequest GET, 'www.staffjoy-v2.local', '/path/1'

        then:
        assertThat(response)
                .hasStatus(status)
                .hasNoBody()

        where:
        status << [OK, NO_CONTENT, FOUND, BAD_REQUEST, NOT_FOUND]
    }


    @Unroll
    def "Should get proxied HTTP response with preserved status when destination response status is internal server error #status"() {

        given:
        stubDestinationResponse status

        when:
        def response = sendRequest GET, 'www.staffjoy-v2.local', '/path/1'

        then:
        assertThat(response)
                .hasStatus(INTERNAL_SERVER_ERROR)
                .hasBody()

        where:
        status << [INTERNAL_SERVER_ERROR]
    }

    @Unroll
    def "Should get proxied HTTP response with #responseHeaders headers when destination response headers are #receivedHeaders"() {
        given:
        stubDestinationResponse receivedHeaders

        when:
        def response = sendRequest GET, 'www.staffjoy-v2.local', '/path/1'

        then:
        assertThat(response)
                  .hasStatus(OK)
                  .containsHeaders(responseHeaders)
                  .notContainsHeaders(removedHeaders)
                  .hasNoBody()

        where:
        receivedHeaders                                                | responseHeaders                                                | removedHeaders
        ['Header-1': 'Value 1']                                        | ['Header-1': 'Value 1']                                        | []
        ['Header-1': 'Value 1', 'Header-2': 'Value 2', 'Header-3': ''] | ['Header-1': 'Value 1', 'Header-2': 'Value 2', 'Header-3': ''] | []
        ['Transfer-Encoding': 'chunked']                               | [:]                                                            | ['Transfer-Encoding']
        ['Public-Key-Pins': 'pin-sha256']                              | [:]                                                            | ['Public-Key-Pins']
        ['Server': 'Apache/2.4.1 (Unix)']                              | [:]                                                            | ['Server']
        ['Strict-Transport-Security': 'max-age=3600']                  | [:]                                                            | ['Strict-Transport-Security']
    }

    def "Should get proxied HTTP response with preserved headers when destination response status indicates error"() {
        given:
        stubDestinationResponse INTERNAL_SERVER_ERROR, ['Header-1': 'Value 1']

        when:
        def response = sendRequest GET, 'www.staffjoy-v2.local', '/path/1'

        then:
        assertThat(response)
                .hasStatus(INTERNAL_SERVER_ERROR)
                .containsHeaders(['Header-1': 'Value 1'])
                .hasBody()
    }

    def "Should get proxied HTTP response with preserved body when destination response body is '#body'"() {
        given:
        stubDestinationResponse body

        when:
        def response = sendRequest GET, 'www.staffjoy-v2.local', '/path/1'

        then:
        assertThat(response)
                 .hasStatus(OK)
                 .hasBody(body)

        where:
        body << [null, '   ', 'Sample body']
    }

    def "Should get proxied HTTP response with preserved body when destination response status indicates error"() {
        given:
        stubDestinationResponse BAD_REQUEST, 'Sample body'

        when:
        def response = sendRequest GET, 'www.staffjoy-v2.local', '/path/1'

        then:
        assertThat(response)
                .hasStatus(BAD_REQUEST)
                .hasBody('Sample body')
    }

    def "Should fail to proxy HTTP request when a timeout occurs"() {
        given:
        stubDestinationResponse true

        when:
        def response = sendRequest GET, 'www.staffjoy-v2.local', '/path/1'

        then:
        assertThat(response)
                .hasStatus(INTERNAL_SERVER_ERROR)
    }
}
