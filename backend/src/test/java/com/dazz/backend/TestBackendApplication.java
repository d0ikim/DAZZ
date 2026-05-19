package com.dazz.backend;

import org.springframework.boot.SpringApplication;

public class TestBackendApplication {

	public static void main(String[] args) {
		SpringApplication.from(DazzApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
