package my.demo.springboot.microservice.todo.it;

import java.util.LinkedHashMap;
import java.util.Map;

public class ServiceEnvironment {

    public static final String TODO_SERVICE_NAME="todo";
    public static final String TODO_SERVICE_VERSION="0.0.1-SNAPSHOT";
    public static final int TODO_SERVICE_PORT=8081;

    public static final String ACCOUNT_SERVICE_NAME="account";
    public static final String ACCOUNT_SERVICE_VERSION="0.0.1-SNAPSHOT";
    public static final int ACCOUNT_SERVICE_PORT=9090;

    public static final String EUREKA_SERVICE_NAME="eureka";
    public static final String EUREKA_SERVICE_VERSION="0.0.1-SNAPSHOT";
    public static final int EUREKA_SERVICE_PORT=8761;

    private static final Map<Instance, SpringBootServiceWrapper> instances = new LinkedHashMap<>();

    static {
        ServiceEnvironment.addEurekaInstance();
        ServiceEnvironment.addAccountInstance();
        ServiceEnvironment.addTodoInstance();
    }

    public static SpringBootServiceWrapper addAccountInstance() {
        ServiceEnvironment.instances
                .put(Instance.ACCOUNT, new SpringBootServiceWrapper(ServiceEnvironment.ACCOUNT_SERVICE_NAME,
                        ServiceEnvironment.ACCOUNT_SERVICE_VERSION, ServiceEnvironment.ACCOUNT_SERVICE_PORT));
        return ServiceEnvironment.instances.get(Instance.ACCOUNT);
    }

    public static SpringBootServiceWrapper addEurekaInstance() {
        ServiceEnvironment.instances
                .put(Instance.EUREKA, new SpringBootServiceWrapper(ServiceEnvironment.EUREKA_SERVICE_NAME,
                        ServiceEnvironment.EUREKA_SERVICE_VERSION, ServiceEnvironment.EUREKA_SERVICE_PORT));
        return ServiceEnvironment.instances.get(Instance.EUREKA);
    }

    public static SpringBootServiceWrapper addTodoInstance() {
        ServiceEnvironment.instances
                .put(Instance.TODO, new SpringBootServiceWrapper(ServiceEnvironment.TODO_SERVICE_NAME,
                        ServiceEnvironment.TODO_SERVICE_VERSION, ServiceEnvironment.TODO_SERVICE_PORT));
        return ServiceEnvironment.instances.get(Instance.TODO);
    }

    public static void startServices() {
        ServiceEnvironment.instances.values().forEach(springBootServiceWrapper -> { springBootServiceWrapper.startService(); });
    }

    public static void shutdownServices() {
        ServiceEnvironment.instances.values().forEach(springBootServiceWrapper -> { springBootServiceWrapper.stopService(); });
    }

    public static SpringBootServiceWrapper getInstance(final Instance instance) {
        return ServiceEnvironment.instances.get(instance);
    }

    public static void removeInstance(final Instance instance) {
        if(ServiceEnvironment.instances.get(instance)!=null) {
            ServiceEnvironment.instances.remove(instance);
        }
    }

}

enum Instance { EUREKA, ACCOUNT, TODO }