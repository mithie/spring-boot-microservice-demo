package my.demo.springboot.microservice.account.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import my.demo.springboot.microservice.account.AccountConfiguration;

@Service
public class AccountService {

    @Autowired
    private AccountConfiguration accountConfiguration;

    public Account findById(final UUID id) {
        final Account account = accountConfiguration.accountRepository().get(id);

        if(account==null) {
            throw new IllegalArgumentException(String.format("Account with id %s not found", id));
        }
        return account;
    }

    public List<Account> findAll() {
        return new ArrayList<>(accountConfiguration.accountRepository().values());
    }
}
