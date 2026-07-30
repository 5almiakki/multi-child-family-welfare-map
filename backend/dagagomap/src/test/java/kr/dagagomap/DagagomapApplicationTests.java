package kr.dagagomap;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles({ "secret", "test" })
class DagagomapApplicationTests {

	@Test
	void contextLoads() {
	}

}
