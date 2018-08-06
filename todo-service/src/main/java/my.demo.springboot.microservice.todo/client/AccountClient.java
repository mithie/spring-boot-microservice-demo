package my.demo.springboot.microservice.todo.client;

import my.demo.springboot.microservice.todo.exception.ClientResponseErrorHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@Component
@Configuration
public class AccountClient {

    private final LoadBalancerClient loadBalancer;
    private final RestTemplateBuilder restTemplateBuilder;
    private final ClientResponseErrorHandler responseErrorHandler;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @LoadBalanced
    @Bean
    RestTemplate restTemplate() {
        return restTemplateBuilder.errorHandler(responseErrorHandler).build();
    }

    @Autowired
    RestTemplate restTemplate;


    @Autowired
    AccountClient(
            final LoadBalancerClient loadBalancer, final RestTemplateBuilder restTemplateBuilder, final ClientResponseErrorHandler responseErrorHandler) {
        this.loadBalancer=loadBalancer;
        this.restTemplateBuilder=restTemplateBuilder;
        this.responseErrorHandler=responseErrorHandler;
    }


    public URI getAccountUri(final UUID accountId) {

        final ServiceInstance instance = loadBalancer.choose("account");
        if (instance == null) {
            return null;
        }

        log.info("Service called on host: {}, port: {}", instance.getHost(), instance.getPort());

        return UriComponentsBuilder.fromHttpUrl( (instance.isSecure() ? "https://" : "http://") +
                instance.getHost() + ":" + instance.getPort() + "/accounts/{id}")
                .buildAndExpand(accountId).toUri();
    }

    public ResponseEntity<Account> getAccount(final URI accountUri) {
        RestTemplate restTemplate = new RestTemplate();

        return restTemplate.getForEntity(accountUri, Account.class);
    }

    public boolean isAccountValid(final UUID accountId) {
        final ResponseEntity<Account> entity = getAccount(getAccountUri(accountId));
        return entity.getStatusCode().is2xxSuccessful();
    }
}