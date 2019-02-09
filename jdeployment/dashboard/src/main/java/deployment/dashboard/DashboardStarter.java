package deployment.dashboard;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import static org.springframework.boot.SpringApplication.run;

@SpringBootApplication
public class DashboardStarter {
    public static void main(String[] args) {
        run(DashboardStarter.class);
    }
}