package smartbell.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    // Kill java process on raspberry: sudo pkill -9 -f Application
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
