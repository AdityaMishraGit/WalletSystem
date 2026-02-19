package com.wallet.ledger.adapter.web;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private int serverPort;

    @Bean
    public OpenAPI walletLedgerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Wallet Ledger API")
                        .description("Production-grade Wallet Ledger. Double-entry accounting; balance from ledger only.")
                        .version("1.0.0")
                        .contact(new Contact().name("Wallet Ledger Team")))
                .servers(List.of(
                        new Server().url("http://localhost:" + serverPort).description("Local"),
                        new Server().url("http://localhost:8080").description("Default")));
    }
}
