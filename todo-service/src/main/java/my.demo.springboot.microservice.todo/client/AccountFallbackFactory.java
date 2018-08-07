package my.demo.springboot.microservice.todo.client;

import feign.hystrix.FallbackFactory;
import my.demo.springboot.microservice.todo.TodoConfiguration;
import my.demo.springboot.microservice.todo.domain.Todo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AccountFallbackFactory implements FallbackFactory<AccountProxy>{

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    TodoConfiguration todoConfiguration;

    @Autowired
    private LoadBalancerClient loadBalancerClient;

    @Override
    public AccountProxy create(final Throwable throwable) {
        return new AccountProxy() {
            @Override
            public ResponseEntity<Account> findById(final UUID id) {
                logger.warn(String.format("findById(%s)", id));

                if (loadBalancerClient.choose("account-service") == null) {
                    if (localFakeCache().get(id) != null) {
                        return ResponseEntity.ok(new Account(id, null, null, null));
                    } else {
                        throw new IllegalArgumentException(String.format("Account with Id %s not found in cache.", id));
                    }
                }
                throw new IllegalArgumentException(throwable);
            }
        };
    }

    private Map<UUID, Todo> localFakeCache() {
        final UUID accountOneId = todoConfiguration.getAccountOneId();
        final UUID accountTwoId = todoConfiguration.getAccountTwoId();

        final Map<UUID, Todo> fallbackMap = new ConcurrentHashMap<>();

        fallbackMap.put(accountOneId, todoConfiguration.todoRepository().get(accountOneId).get(0));
        fallbackMap.put(accountTwoId, todoConfiguration.todoRepository().get(accountTwoId).get(0));

        return fallbackMap;
    }
}
