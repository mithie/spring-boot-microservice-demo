package my.demo.springboot.microservice.todo.client;

import my.demo.springboot.microservice.todo.domain.Todo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Component
public class AccountClient {

    private final DiscoveryClient discoveryClient;

    @Autowired
    AccountClient(final DiscoveryClient discoveryClient) {
        this.discoveryClient=discoveryClient;
    }

    public URI getAccountUri(final Long accountId) {

        final ServiceInstance instance = discoveryClient.getInstances("account-service").get(0);
        if (instance == null)
            return null;

        return UriComponentsBuilder.fromHttpUrl( (instance.isSecure() ? "https://" : "http://") +
                instance.getHost() + ":" + instance.getPort() + "/accounts/{id}")
                .buildAndExpand(accountId).toUri();
    }

    public ResponseEntity<Account> getAccount(final URI accountUri) {
        final RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForEntity(accountUri, Account.class);
    }

    public boolean isAccountValid(final Long accountId) {
        final ResponseEntity<Account> entity = getAccount(getAccountUri(accountId));
        return entity.getStatusCode().is2xxSuccessful();
    }
}