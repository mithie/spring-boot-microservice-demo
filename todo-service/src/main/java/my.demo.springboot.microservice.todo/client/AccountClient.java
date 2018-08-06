package my.demo.springboot.microservice.todo.client;

import java.net.URI;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import my.demo.springboot.microservice.todo.exception.ClientResponseErrorHandler;

@Component
public class AccountClient {

    private final DiscoveryClient discoveryClient;
    private final RestTemplateBuilder restTemplateBuilder;
    private final ClientResponseErrorHandler responseErrorHandler;

    @Autowired
    AccountClient(
            final DiscoveryClient discoveryClient, final RestTemplateBuilder restTemplateBuilder, final ClientResponseErrorHandler responseErrorHandler) {
        this.discoveryClient=discoveryClient;
        this.restTemplateBuilder=restTemplateBuilder;
        this.responseErrorHandler=responseErrorHandler;
    }

    public URI getAccountUri(final UUID accountId) {

        final ServiceInstance instance = discoveryClient.getInstances("account-service").get(0);
        if (instance == null) {
            return null;
        }

        return UriComponentsBuilder.fromHttpUrl( (instance.isSecure() ? "https://" : "http://") +
                instance.getHost() + ":" + instance.getPort() + "/accounts/{id}")
                .buildAndExpand(accountId).toUri();
    }

    public ResponseEntity<Account> getAccount(final URI accountUri) {
        //RestTemplate restTemplate = new RestTemplate();
        final RestTemplate restTemplate = restTemplateBuilder.errorHandler(responseErrorHandler).build();
        return restTemplate.getForEntity(accountUri, Account.class);
    }

    public boolean isAccountValid(final UUID accountId) {
        final ResponseEntity<Account> entity = getAccount(getAccountUri(accountId));
        return entity.getStatusCode().is2xxSuccessful();
    }
}