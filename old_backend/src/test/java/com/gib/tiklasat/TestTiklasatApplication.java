package com.gib.tiklasat;

import org.springframework.boot.SpringApplication;

public class TestTiklasatApplication {

	public static void main(String[] args) {
		SpringApplication.from(TiklasatApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
