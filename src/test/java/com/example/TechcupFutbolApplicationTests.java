package com.example;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled("Requiere BD - usar perfil de integración")
@SpringBootTest
class TechcupFutbolApplicationTests {

	@Test
	void contextLoads() {
        TechcupFutbolApplication.main(new String[]{});
	}

}
