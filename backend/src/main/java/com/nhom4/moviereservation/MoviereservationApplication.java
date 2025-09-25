package com.nhom4.moviereservation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MoviereservationApplication {

	public static void main(String[] args) {
		SpringApplication.run(MoviereservationApplication.class, args);
		//String url = "http://localhost:8080/";
		// String swaggerUrl = "http://localhost:8080/swagger-ui.html";
		//System.out.println("Opening browser to: " + url);
		//openBrowser(url);
		// openBrowser(swaggerUrl);
	}
	private static void openBrowser(String url) {
		try {
			String os = System.getProperty("os.name").toLowerCase();
			if (os.contains("win")) {
				// Sử dụng lệnh rundll32 để mở trình duyệt trên Windows
				Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
			} else if (os.contains("mac")) {
				// Sử dụng lệnh open trên macOS
				Runtime.getRuntime().exec("open " +  url);
			} else if (os.contains("nix") || os.contains("nux")) {
				// Sử dụng lệnh xdg-open trên Linux
				Runtime.getRuntime().exec("xdg-open " + url);
			} else {
				System.err.println("Unsupported operating system.");
			}
		} catch (Exception e) {
			System.err.println("Failed to open browser: " + e.getMessage());
		}
	}

}
