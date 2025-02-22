package com.artipro.configuration;

import com.artipro.exception.PubMedApiException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        // Timeout ayarları
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        //factory.setReadTimeout(5000);

        restTemplate.setRequestFactory(factory);

        // Güncel error handler implementasyonu
//        restTemplate.setErrorHandler(new ResponseErrorHandler() {
//            @Override
//            public boolean hasError(ClientHttpResponse response) {
//                try {
//                    return response.getStatusCode().is4xxClientError() ||
//                            response.getStatusCode().is5xxServerError();
//                } catch (IOException e) {
//                    return true;
//                }
//            }
//        });

        return restTemplate;
    }
}