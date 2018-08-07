package my.demo.springboot.microservice.todo.it;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PathUtils {
    private static final String ROOT_PATH = "spring-boot-microservice-demo";
    private static String PATH_REGEXP = "^(.*?\\/%s).*?$";

    public static String getProjectRoot() {
        Pattern projectPath = Pattern.compile(String.format(PATH_REGEXP, ROOT_PATH));

        final String path = PathUtils.class.getResource(".").getFile();
        final Matcher matcher = projectPath.matcher(path);
        boolean isValid = matcher.matches();
        if (!isValid) {
            throw new IllegalStateException("Path is invalid: " + path);
        }
        return matcher.group(1);
    }
}
