package my.demo.springboot.microservice.todo.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AccountClient {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private LoadBalancerClient loadBalancerClient;

    @Autowired
    AccountProxy accountProxy;

    public boolean isAccountValid(final UUID accountId) {
        logger.info(String.format("isAccountValid(%s)", accountId));

        logAccess();

        return accountProxy.findById(accountId).getStatusCode().is2xxSuccessful();
    }

    private void logAccess() {
        ServiceInstance instance = loadBalancerClient.choose("account-service");

        if(instance != null) {
            logger.info("logAccess(): Service {} called on host: {}, port: {}", instance.getServiceId(), instance.getHost(), instance.getPort());
        } else {
            logger.error("logAccess(): No services available!");
        }
    }
}