package com.back

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@SpringBootTest
internal class BackApplicationTests {
    @Test
    fun contextLoads() {
    }
}
