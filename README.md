# 🏥 Sus Connect - LiveOps Service

## ⏱️ Sobre o Projeto

O **LiveOps Service** é o coração operacional e de monitoramento em
tempo real do ecossistema Sus Connect.

Responsável por processar o alto volume de atualizações de status de
pacientes, ele gerencia as filas de atendimento e fornece indicadores
cruciais (como Tempo Médio de Espera e Ocupação) para o motor de decisão
(Traffic Service).

Diferente dos outros serviços, o LiveOps foi desenhado para alta
performance de escrita e leitura, utilizando banco de dados NoSQL para
suportar a dinamicidade dos pronto-socorros.


### 🚀 Funcionalidades Principais

-   Monitoramento em Tempo Real (Aguardando, Em Atendimento,
    Finalizado)
-   Cálculo de Indicadores (TMA, Tempo Médio de Espera, Fila,
    Ocupação)
-   API de Alta Disponibilidade preparada para Cloud Run
-   Validação JWT via criptografia assimétrica (RSA Public Key)
-   Integração com Traffic Service


## 🛠️ Tecnologias

-   Java 21
-   Spring Boot 3.4.x
-   MongoDB
-   Spring Data MongoDB
-   Spring Security + OAuth2 Resource Server
-   SpringDoc OpenAPI
-   Docker
-   Google Cloud Run
-   JUnit 5 + Mockito


## 📂 Estrutura do Projeto

src/main/java/com/fiap/sus/liveops/

```
src/main/java/com/fiap/sus/liveops/
├── core/                     # Núcleo da aplicação (Configurações Globais)
│   ├── config/               # Configs (Swagger, CORS, Async)
│   ├── exception/            # Tratamento global de erros
│   ├── migrations/           # Scripts de migração do banco (Mongock)
│   └── security/             # Configuração de segurança (Resource Server, JWT)
├── modules/                  # Módulos de Domínio (Features)
│   ├── analytics/            # Módulo de Inteligência e Métricas
│   │   ├── controller/       # Endpoints de leitura de dados
│   │   ├── dto/              # Objetos de transferência de métricas
│   │   └── service/          # Lógica de cálculo de SLA e médias
│   └── attendance/           # Módulo de Atendimento (Operacional)
│       ├── controller/       # Endpoints de triagem e fluxo
│       ├── document/         # Documentos MongoDB
│       │   └── embedded/     # Objetos embutidos
│       ├── dto/              # DTOs de entrada e saída
│       ├── mapper/           # MapStruct para conversão DTO <-> Document
│       ├── repository/       # Interfaces Spring Data MongoDB
│       └── service/          # Regras de negócio de triagem
└── shared/                   # Recursos compartilhados
    └── enums/                # Enumeradores globais
```

## 🚀 Como Executar

### Pré-requisitos

-   Docker
-   Gradle 8+
-   JDK 21
-   Chave Pública do Network Service

### 1. Subir MongoDB

O projeto utiliza Docker Compose para gerenciar as dependências de infraestrutura.
```bash
docker-compose up -d
```

Este comando iniciará:
- **MongoDB**: Porta `27017` (Banco: `susconnect_liveops`)

### 2. Executar Backend

```bash
./gradlew bootRun
```

Disponível em: http://localhost:8080

As migrações do banco de dados são executadas automaticamente pelo **Mongock** ao iniciar a aplicação.


## 🔐 Segurança

O serviço atua como Resource Server.

Fluxo:

1.  O token JWT é gerado a partir de pares de chaves, como os configurados para o `Network Service` ou `Traffic Intelligence Service`
2.  É enviado no header Authorization: Bearer `<token>`
3.  A API LiveOps valida assinatura RSA e expiração


## 📍 Endpoints Principais

**POST /attendances/triage**
-   Descrição: Inicia o processo de triagem para um paciente, registrando seu status como "WAITING".

**PATCH /attendances/{id}**
-   Descrição: Atualiza o status de um atendimento (e.g., "IN_PROGRESS", "DISCHARGED") e registra timestamps para cálculo de indicadores.

**POST /analytics**
-   Descrição: Endpoint para cálculos de indicadores, como Tempo Médio de Atendimento (TMA) e outras métricas de uma lista de unidades, com base nos dados de atendimentos.

Para mais informações sobre os endpoints disponíveis, consulte a documentação Swagger: http://localhost:8080/swagger-ui.html

## ☁️ Deploy GCP

O serviço está preparado para ser implantado no Google Cloud Run, utilizando o `Dockerfile` e as configurações de build do `cloudbuild.yaml`.

**Arquivos de configuração para deploy:**

-   Dockerfile
-   cloudbuild.yaml
-   application-cloud.yml

**SECRETS necessários:**

-   NETWORK_PUBLIC_KEY
-   TRAFFIC_PUBLIC_KEY
-   SPRING_DATA_MONGODB_URI 
  - SWAGGER_SERVER_URL


## 📄 Licença

Desenvolvido por **Fiap-Sptechers** como parte do projeto integrador de Saúde Pública.