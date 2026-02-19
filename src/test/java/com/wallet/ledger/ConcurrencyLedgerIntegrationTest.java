//package com.wallet.ledger;
//
//import org.junit.jupiter.api.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.context.annotation.Import;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.DynamicPropertyRegistry;
//import org.springframework.test.context.DynamicPropertySource;
//import org.springframework.test.web.servlet.MockMvc;
//import org.testcontainers.containers.PostgreSQLContainer;
//import org.testcontainers.junit.jupiter.Container;
//import org.testcontainers.junit.jupiter.Testcontainers;
//import org.testcontainers.utility.DockerImageName;
//
//import java.math.BigDecimal;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.CyclicBarrier;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@Testcontainers
//@SpringBootTest
//@AutoConfigureMockMvc
//@ActiveProfiles("test")
//@Import(TestConfig.class)
//@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//class ConcurrencyLedgerIntegrationTest {
//
//    @Container
//    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine"))
//            .withDatabaseName("wallet_ledger")
//            .withUsername("postgres")
//            .withPassword("postgres");
//
//    @DynamicPropertySource
//    static void configure(DynamicPropertyRegistry registry) {
//        registry.add("spring.datasource.url", () -> "jdbc:postgresql://" + postgres.getHost() + ":" + postgres.getFirstMappedPort() + "/wallet_ledger");
//        registry.add("spring.datasource.username", postgres::getUsername);
//        registry.add("spring.datasource.password", postgres::getPassword);
//        registry.add("spring.flyway.url", () -> "jdbc:postgresql://" + postgres.getHost() + ":" + postgres.getFirstMappedPort() + "/wallet_ledger");
//        registry.add("spring.flyway.user", postgres::getUsername);
//        registry.add("spring.flyway.password", postgres::getPassword);
//    }
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    private static String walletAId;
//    private static String walletBId;
//
//    @Order(1)
//    @Test
//    void createWalletsAndCashIn() throws Exception {
//        String respA = mockMvc.perform(post("/api/v1/wallets")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("{\"userId\":\"user-a\",\"currency\":\"USD\"}"))
//                .andExpect(status().isOk())
//                .andReturn().getResponse().getContentAsString();
//        walletAId = parseWalletId(respA);
//
//        String respB = mockMvc.perform(post("/api/v1/wallets")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("{\"userId\":\"user-b\",\"currency\":\"USD\"}"))
//                .andExpect(status().isOk())
//                .andReturn().getResponse().getContentAsString();
//        walletBId = parseWalletId(respB);
//
//        Assertions.assertNotNull(walletAId);
//        Assertions.assertNotNull(walletBId);
//
//        mockMvc.perform(post("/api/v1/cashin")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("{\"walletId\":\"" + walletAId + "\",\"amount\":100.00}"))
//                .andExpect(status().isOk());
//    }
//
//    @Order(2)
//    @Test
//    void concurrentTransfersNoDoubleDebit() throws InterruptedException {
//        Assumptions.assumeTrue(walletAId != null && walletBId != null);
//        int numThreads = 8;
//        CyclicBarrier barrier = new CyclicBarrier(numThreads);
//        List<Thread> threads = new ArrayList<>();
//        for (int i = 0; i < numThreads; i++) {
//            threads.add(new Thread(() -> {
//                try { barrier.await(); } catch (Exception e) { throw new RuntimeException(e); }
//                try {
//                    String body = "{\"fromWalletId\":\"" + walletAId + "\",\"toWalletId\":\"" + walletBId + "\",\"amount\":15.00}";
//                    mockMvc.perform(post("/api/v1/transfer")
//                                    .contentType(MediaType.APPLICATION_JSON)
//                                    .content(body))
//                            .andExpect(result -> {
//                                int code = result.getResponse().getStatus();
//                                Assertions.assertTrue(code == 200 || code == 422, "expected 200 or 422, got " + code);
//                            });
//                } catch (Exception ignored) { }
//            }));
//        }
//        threads.forEach(Thread::start);
//        for (Thread t : threads) t.join(10000);
//
//        String balanceAResult = mockMvc.perform(get("/api/v1/balance/" + walletAId))
//                .andExpect(status().isOk())
//                .andReturn().getResponse().getContentAsString();
//        String balanceBResult = mockMvc.perform(get("/api/v1/balance/" + walletBId))
//                .andExpect(status().isOk())
//                .andReturn().getResponse().getContentAsString();
//
//        BigDecimal balanceA = parseBalance(balanceAResult);
//        BigDecimal balanceB = parseBalance(balanceBResult);
//
//        Assertions.assertNotNull(balanceA);
//        Assertions.assertNotNull(balanceB);
//        Assertions.assertTrue(balanceA.compareTo(BigDecimal.ZERO) >= 0, "Balance A must not be negative");
//        Assertions.assertEquals(
//                balanceA.add(balanceB).setScale(2, java.math.RoundingMode.HALF_UP),
//                new BigDecimal("100.00"),
//                "Total money must be conserved (100)");
//    }
//
//    private static String parseWalletId(String json) {
//        int i = json.indexOf("\"walletId\":\"");
//        if (i < 0) return null;
//        int start = i + 12;
//        int end = json.indexOf("\"", start);
//        return end > start ? json.substring(start, end) : null;
//    }
//
//    private static BigDecimal parseBalance(String json) {
//        int i = json.indexOf("\"balance\":");
//        if (i < 0) return null;
//        int start = i + 10;
//        int end = json.indexOf("}", start);
//        if (end < 0) end = json.length();
//        String num = json.substring(start, end).trim();
//        return new BigDecimal(num);
//    }
//}
