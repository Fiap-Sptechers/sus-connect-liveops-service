package com.fiap.sus.liveops.core.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${swagger.server.url}")
    private String swaggerServerUrl;

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${spring.profiles.active:}")
    private String activeProfile;

    @Bean
    public GroupedOpenApi liveopsApi() {
        return GroupedOpenApi.builder()
                .group("sus-connect-liveops")
                .packagesToScan("com.fiap.sus.liveops.modules")
                .build();
    }

    @Bean
    public OpenAPI liveopsOpenAPI() {
        List<Server> servers = new ArrayList<>();
        if ("cloud".equals(activeProfile)) {
            servers.add(new Server().url("/").description("Cloud Run (Default)"));
            if (swaggerServerUrl != null && !swaggerServerUrl.isBlank()) {
                servers.add(new Server().url(swaggerServerUrl).description("Cloud Run (Https)"));
            }
        } else {
            servers.add(new Server().url("http://localhost:" + serverPort).description("Local Server"));
        }

        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("SUS Connect - LiveOps Service")
                        .version("1.0.0")
                        .description("""
                            ## Sistema de Monitoramento em Tempo Real (LiveOps)
                            Responsável pela gestão de status de ocupação, tempo de espera e atendimentos ativos.
                           \s
                            ### Segurança
                            Esta API utiliza validação de tokens JWT assinados via RSA-256.\s
                            O token deve ser gerado pelo **Network Service** ou **Traffic Intelligence Service**.
                           \s""")
                        .contact(new Contact().name("SusConnect Team").url("https://github.com/Fiap-Sptechers")))
                .servers(servers)
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }

}
