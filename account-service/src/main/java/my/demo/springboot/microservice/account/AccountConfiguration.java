package my.demo.springboot.microservice.account;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.context.annotation.Configuration;

import my.demo.springboot.microservice.account.domain.Account;

@Configuration
public class AccountConfiguration {

    private Map<UUID, Account> accountRepository = null;

    final UUID accountOneId = UUID.fromString("4e696b86-257f-4887-8bae-027d8e883638");
    final UUID accountTwoId = UUID.fromString("a52dc637-d932-4998-bb00-fe7f248319fb");

    public AccountConfiguration() {
        final Stream<String>
                accountStream = Stream.of(accountOneId.toString() + ",John,Doe,John.Doe@foo.bar", accountTwoId.toString() + ",Jane,Doe,Jane.Doe@foo.bar");

        accountRepository = accountStream.map(account -> {
            String[] info = account.split(",");
            return new Account(UUID.fromString(info[0]), info[1], info[2], info[3]);
        }).collect(Collectors.toMap(Account::getAccountId, usr -> usr));
    }

    public Map<UUID, Account> accountRepository() {
        return accountRepository;
    }
}
