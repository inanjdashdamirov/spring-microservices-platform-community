package msp.community.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "jwt.secret=very-secure-secret-key-change-this-later-very-long"
})
class GatewayServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
