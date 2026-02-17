package com.cdq.assistant.tool;

import com.cdq.assistant.service.CountryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CountryToolTest {

    @Mock
    private CountryService countryService;

    @InjectMocks
    private CountryTool countryTool;

    @Test
    void shouldReturnCountryData() {
        // given
        String expectedJson = "[{\"name\":{\"common\":\"Germany\"}}]";
        when(countryService.fetchCountryData("Germany")).thenReturn(expectedJson);

        // when
        String result = countryTool.getCountryData("Germany");

        // then
        assertThat(result).isEqualTo(expectedJson);
    }

    @Test
    void shouldReturnErrorMessageWhenServiceFails() {
        // given
        when(countryService.fetchCountryData("Unknown"))
                .thenThrow(new RuntimeException("API error"));

        // when
        String result = countryTool.getCountryData("Unknown");

        // then
        assertThat(result).startsWith("Error fetching country data:").contains("API error");
    }
}
