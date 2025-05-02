package com.weather.forecast.config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI weatherForecastOpenAPI() {
        return new OpenAPI().info(new Info()
                        .title("Weather Forecast API")
                        .description("API for retrieving weather forecasts")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Ridwan Waheed")
                                .email("waheedridwan96@gmail.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server().url("/").description("Default Server URL")
                ));
    }
}
