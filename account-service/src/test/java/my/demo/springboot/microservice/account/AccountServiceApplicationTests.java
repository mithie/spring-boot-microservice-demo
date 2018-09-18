package my.demo.springboot.microservice.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import my.demo.springboot.microservice.account.domain.Account;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {AccountApplicationConfiguration.class, WebConfiguration.class})
public class AccountServiceApplicationTests {

	private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private JacksonTester<Account> jacksonTester;

	private Account account;

    private final UUID accountOneId = UUID.fromString("4e696b86-257f-4887-8bae-027d8e883638");

	@Before
	public void setup() {
        ObjectMapper objectMapper = new ObjectMapper();
        JacksonTester.initFields(this, objectMapper);

        mockMvc = webAppContextSetup(webApplicationContext).build();
	    account = new Account("John", "Doe", "john.doe@foo.com");
	}

	@Test
	public void testPostAccountSuccess() throws Exception {
		mockMvc.perform(post("/accounts").contentType("application/hal+json")
                .content(jacksonTester.write(account).getJson())).andExpect(status().isCreated());
	}

	@Test
	public void testDeleteAccountDenied() throws Exception {
		mockMvc.perform(delete("/accounts/"+accountOneId).
				contentType("application/hal+json")).andExpect(status().is(405));
	}

	@Test
	public void testGetAccountResponseEqualsSample() throws Exception {

		final ResultActions result = mockMvc.perform(get("/accounts/"+accountOneId));

		result.andExpect(jsonPath("firstName", is(account.getFirstName())))
				.andExpect(jsonPath("lastName", is(account.getLastName())))
				.andExpect(jsonPath("email", is(account.getEmail())))
				.andExpect((jsonPath("_links.self.href", containsString("accounts/" + accountOneId))));
	}
}
