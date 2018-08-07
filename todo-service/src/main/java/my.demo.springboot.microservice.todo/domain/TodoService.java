package my.demo.springboot.microservice.todo.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import my.demo.springboot.microservice.todo.TodoConfiguration;
import my.demo.springboot.microservice.todo.client.AccountClient;
import my.demo.springboot.microservice.todo.client.AccountProxy;

@Service
public class TodoService {

    @Autowired
    private AccountClient accountClient;

    @Autowired
    AccountProxy accountProxy;

    @Autowired
    TodoConfiguration todoConfiguration;

    public List<Todo> findAll() {
        return todoConfiguration.todoRepository().entrySet().stream().flatMap(l -> l.getValue().stream()).collect(Collectors.toList());
    }

    public List<Todo> findAllById(final UUID accountId) {
        if (!accountClient.isAccountValid(accountId)) {
            throw new IllegalArgumentException(String.format("Account with id %s does not exist!", accountId));
        }
        return todoConfiguration.todoRepository().get(accountId);
    }

    public Todo addTodo(final Todo todo) {
        final Todo created = new Todo(UUID.randomUUID(), todo.getAccountId(), todo.getEmail(), todo.getDescription(), todo.isCompleted());

        List<Todo> todos = findAllById(created.getAccountId());
        if(todos==null) {
            todos = new ArrayList<>();
        }

        if(todos.stream().filter(t -> t.equals(created)).count()==1) {
            throw new IllegalArgumentException("Todo " + created + " already exists");
        }

        todoConfiguration.todoRepository().get(created.getAccountId()).add(created);

        return created;
    }
}