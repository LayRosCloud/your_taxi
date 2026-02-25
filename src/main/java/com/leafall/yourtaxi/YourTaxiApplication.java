package com.leafall.yourtaxi;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class YourTaxiApplication {

	public static void main(String[] args) {
		new SpringApplicationBuilder(YourTaxiApplication.class)
				.bannerMode(Banner.Mode.OFF)
				.run(args);
	}

}
