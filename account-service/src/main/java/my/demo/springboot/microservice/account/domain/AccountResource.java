package my.demo.springboot.microservice.account.domain;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import org.springframework.hateoas.ResourceSupport;

import lombok.Getter;
import my.demo.springboot.microservice.account.api.AccountController;

@Getter
public class AccountResource extends ResourceSupport{
    private final Account account;

    public AccountResource(final Account account) {
        this.account=account;

        add(linkTo(methodOn(AccountController.class).findAll()).withRel("accounts"));
        add(linkTo(methodOn(AccountController.class).findById(account.getAccountId())).withSelfRel());
    }
}