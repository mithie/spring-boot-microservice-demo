package my.demo.springboot.microservice.account.domain;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

@Service
public class AccountService {

    private static Map<Long, Account> accountRepository = null;


    static {
        Stream<String> accountStream = Stream.of("1,John,Doe,John.Doe@foo.bar", "2,Jane,Doe,Jane.Doe@foo.bar");
        accountRepository = accountStream.map(account -> {
                String[] info = account.split(",");
        return new Account(new Long(info[0]), info[1], info[2], info[3]);
        }).collect(Collectors.toMap(Account::getId, usr -> usr));
    }

    public Account findById(Long id) {
        return accountRepository.get(id);
    }
    public Collection<Account> findAll() {
        return accountRepository.values();
    }
}
