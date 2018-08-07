package my.demo.springboot.microservice.todo.domain;

import lombok.*;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = "todoId")
@ToString(exclude = "todoId")
@RequiredArgsConstructor
public class Todo {
    private UUID todoId;

    @NonNull
    private UUID accountId;

    @NonNull
    private String email;

    @NonNull
    private String description;

    @NonNull
    boolean completed;
}
