package my.demo.springboot.microservice.todo.domain;

import lombok.Getter;
import my.demo.springboot.microservice.todo.api.TodoController;
import org.springframework.hateoas.ResourceSupport;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Getter
public class TodoResource extends ResourceSupport{

    private final Todo todo;

    public TodoResource(final Todo todo) {
        this.todo=todo;

        add(linkTo(methodOn(TodoController.class).findAllByAccountId(todo.getAccountId())).withSelfRel());
    }
}
