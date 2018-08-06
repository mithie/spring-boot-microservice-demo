package my.demo.springboot.microservice.todo.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import my.demo.springboot.microservice.todo.client.AccountClient;

//@Service
@Component
public class TodoService {

    private Map<UUID, List<Todo>> todoRepository = null;

    private final List<Todo> todos;

    private final UUID accountOneId = UUID.fromString("4e696b86-257f-4887-8bae-027d8e883638");
    private final UUID accountTwoId = UUID.fromString("a52dc637-d932-4998-bb00-fe7f248319fb");

    @Autowired
    private AccountClient accountClient;


    public TodoService() {
        final Stream<String> todoStream = Stream.of(accountOneId.toString() + ",John.Doe@foo.bar,Clean Dishes,false",
                accountTwoId.toString() + ",Jane.Doe@foo.bar,Pay Bills,false");

        todos = todoStream.map(todo -> {
            String[] info = todo.split(",");
            return new Todo(UUID.randomUUID(), UUID.fromString(info[0]), info[1], info[2], Boolean.getBoolean(info[3]));
        }).collect(Collectors.toCollection(ArrayList::new));

        todoRepository = todos.stream()
                .collect(Collectors.groupingBy(Todo::getAccountId));

    }

    public List<Todo> findAllById(final UUID accountId) {
        if(!accountClient.isAccountValid(accountId)) {
            throw new IllegalArgumentException(String.format("Account with id %s does not exist!", accountId));
        }
        return todoRepository.get(accountId);
    }

    public List<Todo> findAll() {
        return todoRepository.entrySet().stream().flatMap(l -> l.getValue().stream()).collect(Collectors.toList());
    }

    public Todo addTodo(final Todo todo) {

        final Todo created = new Todo(UUID.randomUUID(), todo.getAccountId(), todo.getEmail(), todo.getDescription(), todo.isCompleted());

        List<Todo> todos = findAllById(created.getAccountId());
        if(todos==null) {
            todos = new ArrayList<>();
        }

        final long exists = todos.stream().filter(t -> t.equals(created)).count();

        if(exists==1) {
            throw new IllegalArgumentException("Todo " + created + " already exists");
        }

        todoRepository.get(created.getAccountId()).add(created);

        return created;
    }
}