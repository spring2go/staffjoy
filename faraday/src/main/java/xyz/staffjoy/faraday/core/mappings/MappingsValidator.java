package xyz.staffjoy.faraday.core.mappings;

import xyz.staffjoy.common.services.ServiceDirectory;
import xyz.staffjoy.faraday.config.MappingProperties;
import xyz.staffjoy.faraday.exceptions.FaradayException;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.*;
import static org.springframework.util.CollectionUtils.isEmpty;

public class MappingsValidator {

    public void validate(List<MappingProperties> mappings) {
        if (!isEmpty(mappings)) {
            mappings.forEach(this::correctMapping);
            int numberOfNames = mappings.stream()
                    .map(MappingProperties::getName)
                    .collect(toSet())
                    .size();
            if (numberOfNames < mappings.size()) {
                throw new FaradayException("Duplicated route names in mappings");
            }
            int numberOfHosts = mappings.stream()
                    .map(MappingProperties::getHost)
                    .collect(toSet())
                    .size();
            if (numberOfHosts < mappings.size()) {
                throw new FaradayException("Duplicated source hosts in mappings");
            }
            mappings.sort((mapping1, mapping2) -> mapping2.getHost().compareTo(mapping1.getHost()));
        }
    }

    protected void correctMapping(MappingProperties mapping) {
        validateName(mapping);
        validateDestinations(mapping);
        validateHost(mapping);
        validateTimeout(mapping);
    }

    protected void validateName(MappingProperties mapping) {
        if (isBlank(mapping.getName())) {
            throw new FaradayException("Empty name for mapping " + mapping);
        }
    }

    protected void validateDestinations(MappingProperties mapping) {
        if (isEmpty(mapping.getDestinations())) {
            throw new FaradayException("No destination hosts for mapping" + mapping);
        }
        List<String> correctedHosts = new ArrayList<>(mapping.getDestinations().size());
        mapping.getDestinations().forEach(destination -> {
            if (isBlank(destination)) {
                throw new FaradayException("Empty destination for mapping " + mapping);
            }
            if (!destination.matches(".+://.+")) {
                destination = "http://" + destination;
            }
            destination = removeEnd(destination, "/");
            correctedHosts.add(destination);
        });
        mapping.setDestinations(correctedHosts);
    }

    protected void validateHost(MappingProperties mapping) {
        if (isBlank(mapping.getHost())) {
            throw new FaradayException("No source host for mapping " + mapping);
        }
    }

    protected void validateTimeout(MappingProperties mapping) {
        int connectTimeout = mapping.getTimeout().getConnect();
        if (connectTimeout < 0) {
            throw new FaradayException("Invalid connect timeout value: " + connectTimeout);
        }
        int readTimeout = mapping.getTimeout().getRead();
        if (readTimeout < 0) {
            throw new FaradayException("Invalid read timeout value: " + readTimeout);
        }
    }
}
