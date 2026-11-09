package ma.eventpro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EventproApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventproApplication.class, args);
    }

}
