package my.demo.springboot.microservice.todo.domain;

import java.util.List;
import java.util.UUID;

public interface TodoService {
    public Todo findById(UUID todoId);

    public List<Todo> findAll();

    public List<Todo> findAllByAccount(UUID accountId);

    public Todo addTodo(final Todo todo);
}