package my.demo.springboot.microservice.todo.persistence;

import my.demo.springboot.microservice.todo.domain.Todo;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.List;
import java.util.UUID;

@RepositoryRestResource
public interface TodoRepository extends CrudRepository<Todo, UUID> {

    @RestResource(path = "accounts", rel = "todosByAccount")
    public List<Todo> findByAccountId(UUID accountId);
}