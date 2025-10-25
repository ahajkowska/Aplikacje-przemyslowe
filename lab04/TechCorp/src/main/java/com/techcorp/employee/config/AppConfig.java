package com.techcorp.employee.config;

import com.google.gson.Gson;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

import java.net.http.HttpClient;

@Configuration
@ImportResource("classpath:employees-beans.xml")
public class AppConfig {

    @Bean
    public HttpClient httpClient() {
        return HttpClient.newHttpClient();
    }

    @Bean
    public Gson gson() {
        return new Gson();
    }
}