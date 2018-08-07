package my.demo.springboot.microservice.todo.exception;

import java.util.Date;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.netflix.hystrix.exception.HystrixRuntimeException;

@ControllerAdvice
@RestController
public class ErrorHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public final ResponseEntity<ErrorResult> handleIllegalArgumetException(IllegalArgumentException exception, WebRequest request) {
        ErrorResult result = new ErrorResult(new Date(), exception.getCause().getMessage(), request.getDescription(false));
        return new ResponseEntity<>(result, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(HystrixRuntimeException.class)
    public final ResponseEntity<ErrorResult> handleHystrixRuntimeException(HystrixRuntimeException exception, WebRequest request) {
        ErrorResult result = new ErrorResult(new Date(), exception.getFallbackException().getCause().getMessage(), request.getDescription(false));
        return new ResponseEntity<>(result, HttpStatus.NOT_FOUND);
    }

}
