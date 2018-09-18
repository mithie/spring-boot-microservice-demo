package my.demo.springboot.microservice.todo.domain;

import lombok.*;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.core.Relation;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = "todoId")
@ToString(exclude = "todoId")
@RequiredArgsConstructor
@Relation(collectionRelation="todos")
public class Todo extends ResourceSupport {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "todoid")
    private UUID todoId;

    @NonNull
    @Column(name = "accountid")
    private UUID accountId;

    @NonNull
    @Column(name = "email")
    private String email;

    @NonNull
    @Column(name = "description")
    private String description;

    @NonNull
    @Column(name = "completed")
    boolean completed;
}
