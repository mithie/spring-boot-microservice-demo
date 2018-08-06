package my.demo.springboot.microservice.account;

import my.demo.springboot.microservice.account.api.AccountController;
import my.demo.springboot.microservice.account.domain.Account;
import my.demo.springboot.microservice.account.domain.AccountService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

@RunWith(SpringRunner.class)
@WebMvcTest(AccountController.class)
public class AccountServiceApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private AccountService accountService;

	private Account account;

    private final UUID accountOneId = UUID.randomUUID();

	@Before
	public void setup() {

	    account = new Account(accountOneId, "John", "Doe", "John.Doe@foo.bar");
	}

	@Test
	public void testGetAccountSuccess() throws Exception {

		given(accountService.findById(accountOneId)).willReturn(account);

		final ResultActions result = mockMvc.perform(get("/accounts/"+accountOneId));
		result.andExpect(status().is2xxSuccessful());
	}

	@Test
	public void testGetAccountResponseEqualsSample() throws Exception {

		given(accountService.findById(accountOneId)).willReturn(account);

		final ResultActions result = mockMvc.perform(get("/accounts/"+accountOneId));

		result.andExpect(jsonPath("account.accountId", is(account.getAccountId().toString())))
				.andExpect(jsonPath("account.firstName", is(account.getFirstName())))
				.andExpect(jsonPath("account.lastName", is(account.getLastName())))
				.andExpect(jsonPath("account.email", is(account.getEmail())))
				.andExpect((jsonPath("_links.accounts.href", containsString("accounts"))))
				.andExpect((jsonPath("_links.self.href", containsString("accounts/" + accountOneId))));
	}

}