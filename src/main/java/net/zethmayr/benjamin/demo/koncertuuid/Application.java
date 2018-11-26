package net.zethmayr.benjamin.demo.koncertuuid;

import lombok.val;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String... args) {
        val app = new SpringApplication(Application.class);
        app.run(args);
    }

    public Application() {

    }
}
