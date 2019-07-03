package xyz.staffjoy.faraday.core.http;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import xyz.staffjoy.faraday.config.MappingProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;
import static org.apache.http.impl.client.HttpClientBuilder.create;

public class HttpClientProvider {
    protected Map<String, RestTemplate> httpClients = new HashMap<>();

    public void updateHttpClients(List<MappingProperties> mappings) {
        httpClients = mappings.stream().collect(toMap(MappingProperties::getName, this::createRestTemplate));
    }

    public RestTemplate getHttpClient(String mappingName) {
        return httpClients.get(mappingName);
    }

    protected RestTemplate createRestTemplate(MappingProperties mapping) {
        CloseableHttpClient client = createHttpClient(mapping).build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(client);
        requestFactory.setConnectTimeout(mapping.getTimeout().getConnect());
        requestFactory.setReadTimeout(mapping.getTimeout().getRead());
        return new RestTemplate(requestFactory);
    }

    protected HttpClientBuilder createHttpClient(MappingProperties mapping) {
        return create().useSystemProperties().disableRedirectHandling().disableCookieManagement();
    }
}
