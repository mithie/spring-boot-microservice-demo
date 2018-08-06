package my.demo.springboot.microservice.todo.exception;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;

@Component
public class ClientResponseErrorHandler implements ResponseErrorHandler {

    @Override
    public boolean hasError(final ClientHttpResponse clientHttpResponse) throws IOException {
        return isError(clientHttpResponse.getStatusCode());
    }

    @Override
    public void handleError(final ClientHttpResponse clientHttpResponse) throws IOException {
        System.out.print(String.format("Response Error: {} {}", clientHttpResponse.getStatusCode(), clientHttpResponse.getStatusText()));
    }

    private boolean isError(final HttpStatus status) {
        final HttpStatus.Series series = status.series();
        return (HttpStatus.Series.CLIENT_ERROR.equals(series)
                || HttpStatus.Series.SERVER_ERROR.equals(series));
    }
}
