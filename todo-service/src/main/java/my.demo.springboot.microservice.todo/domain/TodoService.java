package my.demo.springboot.microservice.todo.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import my.demo.springboot.microservice.todo.client.AccountClient;

@Service
public class TodoService {

    private Map<Long, List<Todo>> todoRepository = null;

    private final List<Todo> todos;

    @Autowired
    private AccountClient accountClient;

    public TodoService() {
        final Stream<String> todoStream = Stream.of("1,1,John.Doe@foo.bar,Clean Dishes,false", "2,1,John.Doe@foo.bar,Pay Bills,false", "3,2,Jane.Doe@foo.bar,Go Shopping,true");

        todos = todoStream.map(todo -> {
            String[] info = todo.split(",");
            return new Todo(new Long(info[0]), new Long(info[1]), info[2], info[3], Boolean.getBoolean(info[4]));
        }).collect(Collectors.toCollection(ArrayList::new));

        todoRepository = todos.stream()
                .collect(Collectors.groupingBy(Todo::getAccountId));

    }

    public List<Todo> findAllById(final Long accountId) {
        if(!accountClient.isAccountValid(accountId)) {
            throw new IllegalArgumentException(String.format("Account with id %s does not exist!", accountId));
        }
        return todoRepository.get(accountId);
    }

    public List<Todo> findAll() {

        return todoRepository.entrySet().stream().flatMap(l -> l.getValue().stream()).collect(Collectors.toList());
    }

    public Todo addTodo(final Todo todo) {

        List<Todo> todos = findAllById(todo.getAccountId());
        if(todos==null) {
            todos = new ArrayList<>();
        }

        if(todos.stream().filter(t -> t.equals(todo)).count()==1) {
            throw new IllegalArgumentException("Todo " + todo + " already exists");
        }

        todoRepository.get(todo.getAccountId()).add(todo);

        return todo;
    }
}
