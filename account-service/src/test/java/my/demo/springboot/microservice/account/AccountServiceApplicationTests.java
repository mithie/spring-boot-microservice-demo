package my.demo.springboot.microservice.account;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import my.demo.springboot.microservice.account.api.AccountController;
import my.demo.springboot.microservice.account.domain.Account;
import my.demo.springboot.microservice.account.domain.AccountService;

@RunWith(SpringRunner.class)
@WebMvcTest(AccountController.class)
public class AccountServiceApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private AccountService accountService;

	private final ObjectMapper mapper = new ObjectMapper();


	@Test
	public void testGetUserSuccess() throws Exception {

		Account user = new Account(new Long(1), "John", "Doe", "John.Doe@foo.bar");

		given(accountService.findById(new Long(1))).willReturn(user);

		String res = mockMvc.perform(get("/accounts/1")
				.content(mapper.writeValueAsString(user))
				.contentType(APPLICATION_JSON)
		).andExpect(status().isOk())
		 .andReturn()
		 .getResponse()
		 .getContentAsString();

		Account response = mapper.readValue(res, Account.class);

		assertEquals(user, response);
	}
}