package com.example.thymeleaftest.controller;

import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.example.thymeleaftest.infra.aws.AWSRequestSigningApacheInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.index.IndexResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1")
public class DataCheckController {
    /**
     * aws 가용 지역
     */
    @Value("${cloud.aws.region.static}")
    private String region;//ap-northeast-2 서울리전

    @GetMapping(value = "/search1")
    public String search1() throws IOException {
        log.info("------------------------------------->search1");
        RestHighLevelClient searchClient = searchClient();

        // Create the document as a hash map
        Map<String, Object> document = new HashMap<>();
        document.put("title", "Walk the Line");
        document.put("director", "James Mangold");
        document.put("year", "2005");
        log.info("------------------------------------->Map");

        // Form the indexing request, send it, and print the response
        IndexRequest request = new IndexRequest("my-index", "_doc", "1").source(document);
        IndexResponse response = searchClient.index(request, RequestOptions.DEFAULT);
        System.out.println("response=========>"+response.toString());
        System.out.println("response Result=========>"+response.getIndex());
        return response.toString();

    }
    private static String serviceName = "es";
    private static String host = "https://search-movies-mfwe2o4qcmopc6nuw3r2mkkyty.ap-northeast-2.es.amazonaws.com";// e.g. https://search-mydomain.us-west-1.es.amazonaws.com 앤드포인트

    static final AWSCredentialsProvider credentialsProvider = new DefaultAWSCredentialsProviderChain();
    private RestHighLevelClient searchClient() {
        log.info("------------------------------------->RestHighLevelClient");
        AWS4Signer signer = new AWS4Signer();
        signer.setServiceName(serviceName);
        signer.setRegionName(region);
        HttpRequestInterceptor interceptor = new AWSRequestSigningApacheInterceptor(serviceName, signer, credentialsProvider);
        return new RestHighLevelClient(RestClient.builder(HttpHost.create(host)).setHttpClientConfigCallback(hacb -> hacb.addInterceptorLast(interceptor)));
    }
}
