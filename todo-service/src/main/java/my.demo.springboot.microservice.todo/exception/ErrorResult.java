package my.demo.springboot.microservice.todo.exception;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorResult {
    private Date timestamp;
    private String message;
    private String details;
}
