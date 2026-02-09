# 📊 Módulo Analytics - Lógica de Negócio e Cálculos

Este documento detalha o funcionamento do módulo de **Analytics** do SusConnect LiveOps. O objetivo deste módulo é fornecer inteligência em tempo real, permitindo que gestores e pacientes visualizem a situação real das unidades de saúde, sem as distorções comuns de médias aritméticas simples.

---

## 1. Premissas do Módulo

Diferente de relatórios gerenciais históricos, o LiveOps foca no **"Agora"**.
* **Janela Deslizante:** O sistema analisa apenas os dados das últimas **12 horas** (Turno Atual).
* **Reatividade:** Eventos recentes têm peso maior do que eventos antigos.
* **Visão Híbrida:** O cálculo considera tanto quem já foi atendido quanto quem *ainda está esperando*.

---

## 2. Algoritmos e Cálculos

### 2.1. Tempo de Espera Efetivo
Para refletir a realidade da sala de espera, o sistema calcula o tempo de espera de forma distinta baseada no status do paciente:

1.  **Pacientes Finalizados/Em Atendimento:**
    * Tempo = `Início do Atendimento - Horário da Triagem`
2.  **Pacientes na Fila (Waiting):**
    * Tempo = `Horário Atual (Agora) - Horário da Triagem`
    * *Impacto:* Se um paciente está esperando há 3 horas, essas 3 horas são contabilizadas na média imediatamente, penalizando a unidade em tempo real.

### 2.2. Média Ponderada Temporal (Time-Weighted Average)
Utilizamos uma média ponderada para garantir que a fila que "andou rápido agora" tenha mais relevância do que a fila que "travou a horas atrás".

$$
\text{Média Ponderada} = \frac{\sum (\text{Tempo} \times \text{Peso})}{\sum \text{Pesos}}
$$

O **Peso ($W$)** é calculado pela recência do evento:

* **Pacientes na Fila (Waiting):** Peso Máximo (**1.0**). Eles representam o gargalo atual.
* **Pacientes Atendidos:** O peso decai conforme o tempo passa:
  $$W = \frac{1}{1 + \Delta t}$$
  *(Onde $\Delta t$ é o número de horas desde que o atendimento ocorreu)*.

> **Exemplo:** Um atendimento finalizado há **10 minutos** tem peso ~0.9. Um atendimento finalizado há **5 horas** tem peso ~0.16.

---

### 2.3. Indicador de Ruptura de SLA (`isSlaBreached`)
Este indicador alerta se a unidade está em estado crítico para uma determinada classificação de risco.

O sistema marca `isSlaBreached = true` se **QUALQUER** uma das condições abaixo for atendida:
1.  **Média da Categoria:** A média ponderada supera o limite do protocolo.
2.  **Caso Crítico Individual:** Existe **pelo menos um paciente** na fila de espera aguardando mais do que o limite permitido (Detector de "Paciente Esquecido").

**Tabela de Limites (Protocolo Adaptado):**

| Classificação (Cor) | Tempo Alvo (Manchester) | Tolerância Sistêmica* | Limite Total (Gatilho) |
| :--- | :--- | :--- | :--- |
| 🔴 **Vermelho** | 0 min (Imediato) | +5 min | **> 5 min** |
| 🟠 **Laranja** | 10 min | +0 min | **> 10 min** |
| 🟡 **Amarelo** | 60 min | +0 min | **> 60 min** |
| 🟢 **Verde** | 120 min | +0 min | **> 120 min** |
| 🔵 **Azul** | 240 min | +0 min | **> 240 min** |

*\* A tolerância de 5 min para casos vermelhos existe para absorver o tempo de deslocamento físico e registro no sistema.*

---

## 3. Arquitetura Técnica

### 3.1. Estrutura de Resposta (JSON)
O endpoint retorna uma estrutura hierárquica contendo a visão macro (manchete) e micro (detalhes por risco).

```json
{
  "healthUnitId": "US-01",
  "generalAverageWaitTimeMinutes": 25,
  "queueSnapshot": {
    "totalPatients": 10,
    "waitingCount": 8,
    "inProgressCount": 2
  },
  "riskPerformance": [
    {
      "risk": "RED",
      "averageWaitTimeMinutes": 12,
      "maxWaitTimeLimit": 0,
      "isSlaBreached": true
    },
    {
      "risk": "ORANGE",
      "averageWaitTimeMinutes": 0,
      "maxWaitTimeLimit": 10,
      "isSlaBreached": false
    },
    {
      "risk": "YELLOW",
      "averageWaitTimeMinutes": 0,
      "maxWaitTimeLimit": 60,
      "isSlaBreached": false
    },
    {
      "risk": "GREEN",
      "averageWaitTimeMinutes": 166,
      "maxWaitTimeLimit": 120,
      "isSlaBreached": true
    },
    {
      "risk": "BLUE",
      "averageWaitTimeMinutes": 25,
      "maxWaitTimeLimit": 240,
      "isSlaBreached": false
    }
  ]
}
```

**Campos Chave:**

- **generalAverageWaitTimeMinutes:** Média ponderada de espera para todos os pacientes.
- **queueSnapshot:** Visão instantânea da situação atual da fila.
- **riskPerformance:** Análise detalhada por classificação de risco, incluindo o status de SLA.
- **isSlaBreached:** Indicador crítico para alertar gestores e pacientes.


### 3.2. Performance e Concorrência (Scatter-Gather Pattern)
Para otimizar a visualização de múltiplas unidades no mapa (ex: o paciente abre o app e vê 20 unidades próximas), o sistema utiliza um padrão de processamento assíncrono.

* **Endpoint:** `POST /analytics`
* **Estratégia:** Scatter-Gather (Espalhar e Reunir).
* **Mecanismo:** Utiliza `CompletableFuture` com um `ThreadPoolTaskExecutor` customizado.

**Configuração do Executor:**
* **Core Pool:** 5 threads (Mínimo de concorrência).
* **Max Pool:** 20 threads (Máximo sob carga).
* **Queue Capacity:** 100 tarefas (Buffer de segurança).
* **Política de Rejeição:** `AbortPolicy` (Falha rápido se sobrecarregado).

**Impacto na Latência:**
Ao solicitar dados de 10 unidades simultaneamente:
* **Sem Async (Sequencial):** Tempo Total = $\sum (\text{Tempo de cada Query})$.
* **Com Async (Paralelo):** Tempo Total = $\max (\text{Tempo da Query mais lenta})$.

### 3.3. Stack Tecnológica
* **Banco de Dados:** MongoDB (NoSQL).
    * *Motivo:* Alta performance de escrita para logs de atendimento e flexibilidade de schema.
    * *Índices:* `healthUnitId` (Hash) e `entryTime` (Range) para otimizar o recorte da janela de tempo.
* **Backend:** Java + Spring Boot 3.
* **Bibliotecas Chave:**
    * `Spring Async`: Gerenciamento de Threads.
    * `Lombok`: Redução de boilerplate.
    * `Java Streams API`: Processamento de coleções e cálculo de médias em memória.

---

## 4. Cenários de Teste de Referência

Estes cenários validam se a lógica de negócios está se comportando conforme o esperado e devem ser usados como base para testes de integração.

### Cenário A: O Efeito "Paciente Esquecido"
* **Contexto:** A unidade está estatisticamente rápida (média baixa), mas **um** paciente de risco `RED` foi esquecido na recepção há 10 minutos.
* **Comportamento Esperado:**
    * A média geral (`generalAverageWaitTimeMinutes`) sobe ligeiramente.
    * O indicador de risco `isSlaBreached` para `RED` torna-se **TRUE** imediatamente.
* **Regra Ativada:** "Caso Crítico Individual" — O sistema detecta outliers na fila, independente da média.

### Cenário B: Recuperação de Caos (Peso Temporal)
* **Contexto:** Às 08:00, a unidade teve um pico de lotação com espera de 3 horas. Agora (12:00), a situação normalizou e os atendimentos levam 15 minutos.
* **Comportamento Esperado:**
    * **Média Aritmética Simples (Incorreta):** Mostraria ~90 min (enviesada pelo passado).
    * **Média Ponderada SusConnect (Correta):** Mostra ~20 min.
* **Regra Ativada:** Decaimento de Peso ($1 / (1 + \Delta t)$). Os dados antigos têm peso irrelevante (0.2) comparado aos dados atuais (1.0).

### Cenário C: Janela de Turno
* **Contexto:** Um atendimento foi finalizado ontem (há 24 horas) com tempo de espera de 5 horas.
* **Comportamento Esperado:**
    * Este dado é **ignorado** completamente.
* **Regra Ativada:** Filtro de Janela Deslizante (`entryTime > NOW - 12h`).

---

## 5. Guia de Configuração

Para alterar os parâmetros sensíveis do algoritmo, ajuste as seguintes constantes no código:

| Parâmetro | Localização (`AnalyticsService.java`) | Descrição | Valor Padrão |
| :--- | :--- | :--- | :--- |
| **Janela de Análise** | `ANALYSIS_WINDOW_HOURS` | Tempo de histórico considerado (em horas). | `12` |
| **Tolerância Emergência** | `tolerance` (método `getAnalytics`) | Tempo extra permitido para burocracia em casos `RED`. | `5 min` |
| **Pesos de Decaimento** | `calculateWeightedWaitTime` | Fórmula matemática de ponderação. | `1.0 / (1.0 + hoursAgo)` |

