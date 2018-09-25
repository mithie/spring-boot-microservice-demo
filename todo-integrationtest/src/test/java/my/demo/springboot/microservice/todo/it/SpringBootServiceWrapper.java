package my.demo.springboot.microservice.todo.it;

import static org.awaitility.Awaitility.await;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.assertj.core.util.Lists;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class SpringBootServiceWrapper {

    private final static Logger logger = LoggerFactory.getLogger(SpringBootServiceWrapper.class);

    private final String serviceName;
    private final String version;
    private final int port;

    public static final long MAX_ENDPOINT_STARTUP_TIME = 60L;
    public static final long MAX_ENDPOINT_SHUTDOWN_TIME = 25L;
    public static final long MAX_ENDPOINT_DEREG_TIME = 60L;

    public SpringBootServiceWrapper(final String serviceName, final String version, final int port) {
        this.serviceName=serviceName;
        this.version=version;
        this.port=port;
    }

    public void startService() {
        final List<String> arguments = Lists.newArrayList("java", "-jar", "-Dserver.port="+port, executable(serviceName, version));
        arguments.addAll(defaultArgs);

        final ProcessBuilder processBuilder = new ProcessBuilder(arguments);
        processBuilder.redirectErrorStream(true);

        final File logFile = new File(logPath(serviceName));
        processBuilder.redirectOutput(ProcessBuilder.Redirect.appendTo(logFile));
        Process service = null;

        try {
            service = processBuilder.start();
            await().atMost(SpringBootServiceWrapper.MAX_ENDPOINT_STARTUP_TIME, TimeUnit.SECONDS).until(endpointIsUp());
        } catch(final Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(String.format("couldn't start %s-service", serviceName));
        }

        if (!service.isAlive()) {
            throw new IllegalStateException(String.format("$s-service not started", serviceName));
        }

        SpringBootServiceWrapper.logger.info(String.format("started %s-service", serviceName));
    }

    public void stopService() {
        try {
            await().atMost(SpringBootServiceWrapper.MAX_ENDPOINT_SHUTDOWN_TIME, TimeUnit.SECONDS).until(endpointIsShutdown());
        } catch(final Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(String.format("couldn't stop %s-service", serviceName));
        }
        SpringBootServiceWrapper.logger.info(String.format("shutdown %s-service", serviceName));
    }

    public Callable<Boolean> endpointIsUp() {
        return endpointState(String.format("http://localhost:%s/actuator/health", port), true);
    }

    public Callable<Boolean> endpointIsDown() {
        return endpointState(String.format("http://localhost:%s/actuator/health", port), false);
    }

    public Callable<Boolean> endpointIsDeregistered(final String endpointUrl) {
        return endpointState(endpointUrl, true);
    }

    public int getPort() {
        return port;
    }

    private final List<String> defaultArgs = Lists.newArrayList(
            "--management.endpoint.shutdown.enabled=true",
            "--management.endpoints.web.exposure.include=health,shutdown",
            "--logging.level.org.springframework=WARN");

    private String executable(final String serviceName, final String version) {
        return PathUtils.getProjectRoot() +
            String.format("/%s-service/target/%s-service-%s.jar",serviceName, serviceName, version);
    }

    private String logPath(final String serviceName) {
        return PathUtils.getProjectRoot() + String.format("/todo-integrationtest/target/log_%s.txt", serviceName);
    }

    private Callable<Boolean> endpointState(final String endpointUrl, final boolean checkIfUp) {
        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    final RestTemplate restTemplate = new RestTemplate();

                    final ResponseEntity<JSONObject> response = restTemplate
                            .getForEntity(endpointUrl, JSONObject.class);

                    final boolean responseSuccessful = response.getStatusCode().is2xxSuccessful();

                    return checkIfUp ? responseSuccessful : !responseSuccessful;
                } catch(final Exception e) {
                    return checkIfUp ? false : true;
                }
            }
        };
    }

    private Callable<Boolean> endpointIsShutdown() {
        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    final RestTemplate restTemplate = new RestTemplate();
                    final String url = String.format("http://localhost:%s/actuator/shutdown", port);

                    final HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);

                    final HttpEntity<String> entity = new HttpEntity<>("", headers);

                    final ResponseEntity<JSONObject> response = restTemplate
                            .postForEntity(url, entity, JSONObject.class);

                    return response.getStatusCode().is2xxSuccessful();
                } catch(final Exception e) {
                    return false;
                }
            }
        };
    }
}
