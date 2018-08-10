package my.demo.springboot.microservice.account.persistence;

import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import my.demo.springboot.microservice.account.domain.Account;

@RepositoryRestResource
public interface AccountRepository extends CrudRepository<Account, UUID> {
    Account findByEmail(String email);

    @Override
    @RestResource(exported = false)
    public void delete(Account account);

    @Override
    @RestResource(exported = false)
    public void deleteById(UUID accountId);

}
