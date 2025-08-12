package gotaxi.uberlikeappbackend;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class UberLikeAppBackendApplication {

    @Value("${my-test-property}")
    private String testProperty;

    public static void main(String[] args) {
        SpringApplication.run(UberLikeAppBackendApplication.class, args);
    }

    @PostConstruct
    public void checkTestProperty() {
        System.out.println("DEBUG: Valor de my-test-property: " + testProperty);
    }

}
