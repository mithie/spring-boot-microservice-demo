package my.demo.springboot.microservice.todo;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.UUID;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.netflix.ribbon.StaticServerList;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerList;

import feign.FeignException;
import my.demo.springboot.microservice.todo.client.Account;
import my.demo.springboot.microservice.todo.client.AccountProxy;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "feign.hystrix.enabled=true",
        "eureka.client.enabled=false"
})
@ContextConfiguration(classes = {TodoFeignApplicationTest.TodoServiceTestConfiguration.class})
public class TodoFeignApplicationTest {

    @ClassRule
    public static WireMockClassRule wiremock = new WireMockClassRule(
            wireMockConfig().dynamicPort());

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Autowired
    private AccountProxy accountProxy;

    @Autowired
    private TodoServiceTestConfiguration testConfig;

    @Test
    public void testFindById() {
        stubFor(get(urlEqualTo("/accounts/" + testConfig.getAccountId()))
            .willReturn(aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeader("Content-Type", "application/hal+json")
                    .withBodyFile("account.json")));

        ResponseEntity<Account> account = accountProxy.findById(testConfig.getAccountId());

        assertNotNull("should not be null", account);
        assertThat(account.getBody().getAccountId(), is(testConfig.getAccountId()));
        assertThat(account.getBody().getFirstName(), is("John"));
        assertThat(account.getBody().getLastName(), is("Doe"));
        assertThat(account.getBody().getEmail(), is("John.Doe@foo.bar"));
    }

    @Test
    public void testFindByIdFails() {
        thrown.expect(HystrixRuntimeException.class);
        thrown.expectCause(isA(FeignException.class));
        thrown.expectMessage("AccountProxy#findById(UUID) failed and fallback failed");

        stubFor(get(urlEqualTo("/accounts/" + testConfig.getNonExistingAccountId()))
                .willReturn(serverError()));

        accountProxy.findById(testConfig.getNonExistingAccountId());;
    }


    @TestConfiguration
    public static class TodoServiceTestConfiguration {
        private final UUID accountId = UUID.fromString("4e696b86-257f-4887-8bae-027d8e883638");
        private final UUID nonExistingAccountId = UUID.fromString("4e696b86-257f-4887-8bae-027d8e883637");

        @Bean
        public ServerList<Server> ribbonServerList() {
            return new StaticServerList<>(new Server("localhost", wiremock.port()));
        }

        public UUID getAccountId() {
            return accountId;
        }

        public UUID getNonExistingAccountId() {
            return nonExistingAccountId;
        }
    }

}
