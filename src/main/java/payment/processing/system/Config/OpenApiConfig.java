package payment.processing.system.Config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI salaryPaymentOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Enterprise Salary Payment Management System")
                        .description("APIs for employee management, salary payments, refunds, scheduling and analytics.")
                        .version("v1.0")
                        .contact(new Contact().name("Payroll Engineering Team")));
    }
}

