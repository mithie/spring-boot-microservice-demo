package my.demo.springboot.microservice.todo.api;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import my.demo.springboot.microservice.todo.domain.Todo;
import my.demo.springboot.microservice.todo.domain.TodoServiceImpl;

@RestController
public class TodoController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private TodoServiceImpl todoServiceImpl;

    @GetMapping(path = "/todos", produces = "application/hal+json")
    public ResponseEntity<Resources<Todo>> findAll(){
        logger.info("findAll()");

        List<Todo> todos = todoServiceImpl.findAll();

        return ResponseEntity.ok(todoResources(todos));
    }

    @GetMapping(path = "/todos/{id}", produces = "application/hal+json")
    public ResponseEntity<Todo> findById(@PathVariable("id") UUID todoId){
        logger.info(String.format("findById(%s)", todoId));

        Todo todo = todoServiceImpl.findById(todoId);

        return ResponseEntity.ok(todo);
    }

    @GetMapping(path = "/accounts/{accountid}/todos", produces = "application/hal+json")
    public ResponseEntity<Resources<Todo>> findAllByAccount(@PathVariable("accountid") UUID accountId){
        logger.info(String.format("findAllByAccount(%s)", accountId));

        List<Todo> todos = todoServiceImpl.findAllByAccount(accountId);

        return ResponseEntity.ok(todoResources(todos));
    }

    @PostMapping(path = "/todos")
    public ResponseEntity<Todo> addTodo(@RequestBody final Todo todo){
        logger.info(String.format("addTodo(%s)", todo));

        Todo result = todoServiceImpl.addTodo(todo);

        final URI uri = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();

        addLinkToSingleElement(result);

        return ResponseEntity.created(uri).body(result);
    }

    private Resources<Todo> todoResources(List<Todo> todos) {
        addLinkToList(todos);
        return new Resources(todos);
    }

    private void addLinkToList(List<Todo> todos) {
            todos.forEach(t-> { addLinkToSingleElement(t);
        });
    }

    private void addLinkToSingleElement(Todo todo) {
        if(!todo.hasLink("self")) {
            todo.add(linkTo(ControllerLinkBuilder.methodOn(TodoController.class).findById(todo.getTodoId())).withSelfRel());
            todo.add(linkTo(ControllerLinkBuilder.methodOn(TodoController.class).findAllByAccount(todo.getAccountId())).withRel("accountTodos"));
            todo.add(linkTo(ControllerLinkBuilder.methodOn(TodoController.class).findAll()).withRel("todos"));
        }
    }
}