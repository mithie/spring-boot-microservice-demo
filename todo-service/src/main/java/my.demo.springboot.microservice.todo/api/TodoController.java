package my.demo.springboot.microservice.todo.api;

import my.demo.springboot.microservice.todo.client.AccountClient;
import my.demo.springboot.microservice.todo.persistence.TodoRepository;
import my.demo.springboot.microservice.todo.domain.Todo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@RepositoryRestController
public class TodoController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final TodoRepository todoRepository;

    private final AccountClient accountClient;

    public TodoController(final TodoRepository todoRepository, final AccountClient accountClient) {
        this.accountClient=accountClient;
        this.todoRepository=todoRepository;
    }

    @GetMapping(path="/todos/search/accounts", produces = "application/hal+json")
    public ResponseEntity<Resources<Todo>> findByAccountId(@RequestParam(value="accountId") final UUID accountId){
        logger.info(String.format("findByAccountId(%s)", accountId));

        throwIfAccountInvalid(accountId);

        return ResponseEntity.ok(todoResources(todoRepository.findByAccountId(accountId)));
    }

    @PostMapping(path = "/todos")
    public ResponseEntity<Todo> addTodo(@RequestBody final Todo todo){
        logger.info(String.format("addTodo(%s)", todo));

        throwIfAccountInvalid(todo.getAccountId());

        Todo created = new Todo(UUID.randomUUID(), todo.getAccountId(), todo.getEmail(), todo.getDescription(), todo.isCompleted());

        throwIfTodoExists(created);

        todoRepository.save(created);

        final URI uri = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();

        addLinkToSingleElement(created);

        return ResponseEntity.created(uri).body(created);
    }

    private void throwIfAccountInvalid(UUID accountId) {
        if (!accountClient.isAccountValid(accountId)) {
            throw new IllegalArgumentException(String.format("Account with id %s does not exist!", accountId));
        }
    }

    private void throwIfTodoExists(Todo todo) {
        List<Todo> todos = todoRepository.findByAccountId(todo.getAccountId());

        if(todos.stream().filter(t -> t.equals(todo)).count()==1) {
            throw new IllegalArgumentException("Todo " + todo + " already exists");
        }
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
            todo.add(linkTo(ControllerLinkBuilder.methodOn(TodoController.class).findByAccountId(todo.getAccountId())).withRel("accountTodos"));
        }
    }
}