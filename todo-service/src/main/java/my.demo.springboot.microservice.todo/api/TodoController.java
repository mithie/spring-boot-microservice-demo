package my.demo.springboot.microservice.todo.api;

import my.demo.springboot.microservice.todo.domain.Todo;
import my.demo.springboot.microservice.todo.domain.TodoResource;
import my.demo.springboot.microservice.todo.domain.TodoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resources;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.hateoas.core.DummyInvocationUtils.methodOn;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@RestController
public class TodoController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private TodoService todoService;


    @RequestMapping(path = "/todos", method = RequestMethod.GET, produces = "application/hal+json")
    public ResponseEntity<Resources<TodoResource>> findAll(){
        logger.info("findAll()");
        List<Todo> todos = todoService.findAll();
        return ResponseEntity.ok(todoResources(todos));
    }

    @RequestMapping(path = "/accounts/{accountid}/todos", method = RequestMethod.GET, produces = "application/hal+json")
    public ResponseEntity<Resources<TodoResource>> findAllByAccountId(@PathVariable("accountid") UUID accountId){
        logger.info(String.format("findAllByAccountId(%s)", accountId));

        List<Todo> todos = todoService.findAllById(accountId);

        return ResponseEntity.ok(todoResources(todos));
    }

    @RequestMapping(path = "/todos", method = RequestMethod.POST, produces = "application/hal+json")
    public ResponseEntity<TodoResource> addTodo(@RequestBody final Todo todo){
        logger.info(String.format("addTodo(%s)", todo));

        Todo result = todoService.addTodo(todo);

        final URI uri = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();
        return ResponseEntity.created(uri).body(new TodoResource(result));
    }

    private Resources<TodoResource> todoResources(List<Todo> todos) {
        final List<TodoResource> todoResources = todos.stream().map(TodoResource::new).collect(Collectors.toList());

        final Resources<TodoResource> resources = new Resources(todoResources);

        resources.add(linkTo(methodOn(TodoController.class).findAll()).withSelfRel());

        return resources;
    }
}