package stv10.mb2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import stv10.mb2.config.ResendProperties;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(ResendProperties.class)
public class Mb2Application {

	public static void main(String[] args) {
		SpringApplication.run(Mb2Application.class, args);
	}

}
