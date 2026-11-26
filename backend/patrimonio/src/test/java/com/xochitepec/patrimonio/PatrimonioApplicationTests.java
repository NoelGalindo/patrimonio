package com.xochitepec.patrimonio;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Import(TestcontainersConfig.class)
@Testcontainers(disabledWithoutDocker = true)
class PatrimonioApplicationTests {

    @Test
    void contextLoads() {
        // Arranca el contexto completo, aplica las migraciones de Flyway y valida el mapeo JPA
    }
}
