package com.cdq.assistant.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class CountryService {

    private final RestTemplate restTemplate;
    private final String countryByNamePath;

    public CountryService(@Qualifier("countryRestTemplate") RestTemplate restTemplate,
                          @Value("${country.api.country-by-name-path}") String countryByNamePath) {
        this.restTemplate = restTemplate;
        this.countryByNamePath = countryByNamePath;
    }

    public String fetchCountryData(String countryName) {
        log.info("Fetching country data for: {}", countryName);
        final String result = restTemplate.getForObject(countryByNamePath, String.class, countryName);
        log.info("Successfully fetched country data for: {}", countryName);
        log.debug("Country data: {}", result);
        return result;
    }
}
