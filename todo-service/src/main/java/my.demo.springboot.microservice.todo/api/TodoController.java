package my.demo.springboot.microservice.todo.api;

import my.demo.springboot.microservice.todo.domain.Todo;
import my.demo.springboot.microservice.todo.domain.TodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
public class TodoController {

    @Autowired
    private TodoService todoService;

    @RequestMapping(path = "/todos", method = RequestMethod.GET)
    public Collection<Todo> findAll(){
        return todoService.findAll();
    }

    @RequestMapping(path = "/accounts/{accountid}/todos", method = RequestMethod.GET)
    public Collection<Todo> findAllByAccountId(@PathVariable("accountid") final Long accountId){
        return todoService.findAllById(accountId);
    }

    @RequestMapping(path = "/todos", method = RequestMethod.POST)
    public Todo addTodo(@RequestBody final Todo todo){
        return todoService.addTodo(todo);
    }
}
