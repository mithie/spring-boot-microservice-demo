package my.demo.springboot.microservice.todo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.context.annotation.Configuration;

import my.demo.springboot.microservice.todo.domain.Todo;

@Configuration
public class TodoConfiguration {

    private Map<UUID, List<Todo>> todoRepository = null;

    private final List<Todo> todos;

    private final UUID accountOneId = UUID.fromString("4e696b86-257f-4887-8bae-027d8e883638");
    private final UUID accountTwoId = UUID.fromString("a52dc637-d932-4998-bb00-fe7f248319fb");


    public TodoConfiguration() {
        final Stream<String>
                todoStream = Stream.of(accountOneId.toString() + ",John.Doe@foo.bar,Clean Dishes,false", accountTwoId.toString() + ",Jane.Doe@foo.bar,Pay Bills,false");

        todos = todoStream.map(todo -> {
            String[] info = todo.split(",");
            return new Todo(UUID.randomUUID(), UUID.fromString(info[0]), info[1], info[2], Boolean.getBoolean(info[3]));
        }).collect(Collectors.toCollection(ArrayList::new));

        todoRepository = todos.stream()
                .collect(Collectors.groupingBy(Todo::getAccountId));
    }

    public Map<UUID, List<Todo>> todoRepository() {
        return todoRepository;
    }

    public UUID getAccountOneId() {
        return accountOneId;
    }

    public UUID getAccountTwoId() {
        return accountTwoId;
    }
}
