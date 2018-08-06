package my.demo.springboot.microservice.account.api;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resources;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import my.demo.springboot.microservice.account.domain.Account;
import my.demo.springboot.microservice.account.domain.AccountResource;
import my.demo.springboot.microservice.account.domain.AccountService;

@RestController
public class AccountController {

    @Autowired
    AccountService accountService;

    @RequestMapping(value = "/accounts/{id}", produces = "application/hal+json")
    public ResponseEntity<AccountResource> findById(@PathVariable final UUID id){
        final Account a = accountService.findById(id);

        final AccountResource ar = new AccountResource(a);
        return ResponseEntity.ok(ar);
    }

    @RequestMapping(value="/accounts", produces = "application/hal+json")
    public ResponseEntity<Resources<AccountResource>> findAll(){
        final List< AccountResource > accounts = accountService.findAll().stream().map(AccountResource::new).collect(
                Collectors.toList());
        final Resources <AccountResource> accountResources = new Resources(accounts);

        final String uriString = ServletUriComponentsBuilder.fromCurrentRequest().build().toUriString();
        accountResources.add(new Link(uriString, "self"));

        return ResponseEntity.ok(accountResources);
    }

    @RequestMapping(value = "/")
    public String home() {
        return "OK";
    }
}
