package com.invoice_scanner_backend;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class InvoiceScannerBackendApplication {

	public static void main(String[] args) {
		// Load environment variables from .env file
		Dotenv dotenv = Dotenv.configure()
				.directory("./")
				.ignoreIfMalformed()
				.ignoreIfMissing()
				.load();
		
		// Set system properties from .env
		dotenv.entries().forEach(entry -> {
			System.setProperty(entry.getKey(), entry.getValue());
		});
		
		SpringApplication.run(InvoiceScannerBackendApplication.class, args);
	}

}
