package com.cdq.assistant.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CountryServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private final String countryByNamePath = "/name/{name}";

    private CountryService countryService;

    @Test
    void shouldReturnCountryData() {
        // given
        countryService = new CountryService(restTemplate, countryByNamePath);
        String expectedJson = "[{\"name\":{\"common\":\"France\"}}]";
        when(restTemplate.getForObject(countryByNamePath, String.class, "France"))
                .thenReturn(expectedJson);

        // when
        String result = countryService.fetchCountryData("France");

        // then
        assertThat(result).isEqualTo(expectedJson);
    }

    @Test
    void shouldPropagateExceptionWhenApiFails() {
        // given
        countryService = new CountryService(restTemplate, countryByNamePath);
        when(restTemplate.getForObject(countryByNamePath, String.class, "Unknown"))
                .thenThrow(new RestClientException("404 Not Found"));

        // when & then
        assertThatThrownBy(() -> countryService.fetchCountryData("Unknown"))
                .isInstanceOf(RestClientException.class)
                .hasMessageContaining("404 Not Found");
    }
}
