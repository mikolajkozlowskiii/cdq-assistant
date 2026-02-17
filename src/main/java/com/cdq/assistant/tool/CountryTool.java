package com.cdq.assistant.tool;

import com.cdq.assistant.service.CountryService;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CountryTool {

    private final CountryService countryService;

    @Tool("Fetches detailed information about a country by name, including capital, population, " +
            "area, region, languages, and currencies")
    public String getCountryData(String countryName) {
        try {
            return countryService.fetchCountryData(countryName);
        } catch (Exception e) {
            log.error("Failed to fetch country data for: {}", countryName, e);
            return "Error fetching country data: " + e.getMessage();
        }
    }
}
