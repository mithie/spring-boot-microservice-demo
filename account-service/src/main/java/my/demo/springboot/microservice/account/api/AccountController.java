package my.demo.springboot.microservice.account.api;

import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import my.demo.springboot.microservice.account.domain.Account;
import my.demo.springboot.microservice.account.persistence.AccountRepository;

@RepositoryRestController
public class AccountController {

    private final AccountRepository accountRepository;

    public AccountController(final AccountRepository accountRepository) {
        this.accountRepository=accountRepository;
    }

    @PostMapping(path = "accounts", produces = "application/hal+json")
    public ResponseEntity<Account> save(@RequestBody Account account) {
        if(accountRepository.findByEmail(account.getEmail())==null) {
            Account created = accountRepository.save(account);
            return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest().build().toUri()).body(created);
        } else {
            throw new IllegalArgumentException("Account with email " + account.getEmail() + " already exists!");
        }
    }
}
