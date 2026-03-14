package com.Guess.Sketch.guess_and_sketch_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GuessAndSketchServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(GuessAndSketchServerApplication.class, args);
	}

}
