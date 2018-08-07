package my.demo.springboot.microservice.todo.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.UUID;

@FeignClient(name = "account-service", fallbackFactory = AccountFallbackFactory.class)
public interface AccountProxy {

    @RequestMapping(value = "/accounts/{id}", produces = "application/hal+json", method= RequestMethod.GET)
    public ResponseEntity<Account> findById(@PathVariable final UUID id);
}
