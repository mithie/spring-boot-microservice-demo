package my.demo.springboot.microservice.account.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

@Service
public class AccountService {

    private static Map<UUID, Account> accountRepository = null;

    static {
        final UUID accountOneId = UUID.fromString("4e696b86-257f-4887-8bae-027d8e883638");
        final UUID accountTwoId = UUID.fromString("a52dc637-d932-4998-bb00-fe7f248319fb");

        final Stream<String> accountStream = Stream.of(accountOneId.toString() + ",John,Doe,John.Doe@foo.bar", accountTwoId.toString() + ",Jane,Doe,Jane.Doe@foo.bar");

        AccountService.accountRepository = accountStream.map(account -> {
                String[] info = account.split(",");
        return new Account(UUID.fromString(info[0]), info[1], info[2], info[3]);
        }).collect(Collectors.toMap(Account::getAccountId, usr -> usr));
    }

    public Account findById(final UUID id) {
        final Account account = AccountService.accountRepository.get(id);

        if(account==null) {
            throw new IllegalStateException(String.format("Account with id %s not found", id));
        }
        return account;
    }

    public List<Account> findAll() {
        return new ArrayList<>(AccountService.accountRepository.values());
    }
}
