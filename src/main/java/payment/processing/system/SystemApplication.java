package payment.processing.system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SystemApplication {

	public static void main(String[] args) {
        System.out.println("Starting Payment Processing System Application...");
		SpringApplication.run(SystemApplication.class, args);
        System.out.println("Payment Processing System Application started successfully.");
	}

}
