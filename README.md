# 💳 ToolsChallenge – Payment API

API REST para **processamento e consulta de pagamentos de cartão**, desenvolvida em **Java 21** com **Spring Boot**, **PostgreSQL**, **Flyway**, testes com **JUnit 5 + Mockito**.

---



## 1. Tecnologias e Dependências

### Linguagem e runtime

- **Java:** 21

### Framework

- **Spring Boot:** 4.0.0  

### Persistência

- **JPA / Hibernate**
  - Entidade: `TransactionEntity`
  - Repositório: `TransactionRepository`
- **Banco de dados:** PostgreSQL
  - Driver: `org.postgresql:postgresql`
  - Uso de sequence para NSU (`seq_nsu`)
- **Migração de schema:** Flyway
  - Scripts em `src/main/resources/db/migration`
  - Script inicial: `init.sql`

### Build

- **Gradle** (Kotlin DSL – `build.gradle.kts`)

### Utilitários

- **Lombok**
  - `@Builder`, `@Getter`, `@Setter`, `@RequiredArgsConstructor`, etc.
- **DevTools**
  - `spring-boot-devtools` habilitado para auto-restart em desenvolvimento

### Testes

- **JUnit 5**
  - `spring-boot-starter-test`
- **Mockito**
  - `org.mockito:mockito-junit-jupiter`

---

## 2. Visão Geral

A API expõe operações para:

- Cadastrar um pagamento de cartão
- Listar todas as transações cadastradas
- Buscar um pagamento por ID
- Estornar (cancelar) um pagamento já autorizado

Todo o fluxo é persistido em banco PostgreSQL, utilizando:

- `TransactionEntity` como entidade de persistência
- Geração de **NSU sequencial** via sequence no banco
- Geração de **código de autorização**
- Enums para **status da transação** e **tipo de forma de pagamento**
- Tratamento centralizado de erros via `GlobalExceptionHandler`

---

## 3. Arquitetura de Pacotes


- `controller`
  - `PaymentController`
    - Endpoints REST em `/pagamentos`
- `service`
  - `PaymentService`
  - `PaymentServiceImpl`
    - Contém as regras de negócio de criação, busca, listagem e estorno.
- `mapper`
  - `TransactionMapper`
  - `TransactionMapperImpl`
    - Responsável por mapear:
      - `TransactionEntity` → `PagamentoResponseDTO`
      - `PagamentoRequestDTO` → `TransactionEntity`
- `repository`
  - `TransactionRepository`
    - CRUD da entidade `TransactionEntity`
    - Método adicional: `Long getNextNsu()`
- `entity`
  - `TransactionEntity`
    - Tabela principal de transações de pagamento
- `enums`
  - `TipoFormaPagamento`
    - `AVISTA(1, "AVISTA")`
    - `PARCELADO_LOJA(2, "PARCELADO LOJA")`
    - `PARCELADO_EMISSOR(3, "PARCELADO EMISSOR")`
  - `StatusTransacao`
    - `AUTORIZADO(1, "AUTORIZADO")`
    - `NEGADO(2, "NEGADO")`
    - `CANCELADO(3, "CANCELADO")`
- `handler`
  - `GlobalExceptionHandler`
    - `@RestControllerAdvice`
    - Trata todas as exceções conhecidas e generiza as desconhecidas
- `exception`
  - `DuplicateTransactionIdException`
  - `PaymentNotFoundException`
  - `InvalidInstallmentsForPaymentTypeException`

---

## 4. Regras de Negócio

### 4.1. Criação de Pagamento (`POST /pagamentos`)

- DTO de entrada: `PagamentoRequestDTO`
- Fluxo principal:
  1. Validação do DTO via Bean Validation (`@Valid` no controller).
  2. Verificação se o ID da transação já existe:
     - `transactionRepository.existsById(transacao.id)`
     - Se existir → `DuplicateTransactionIdException` → HTTP 409.
  3. Mapeamento DTO → Entity via `TransactionMapperImpl.toTransactionEntityRequest`.
  4. Geração de NSU via sequence (`getNextNsu()`).
  5. Geração de código de autorização via `UUID`.
  6. Status inicial: `AUTORIZADO`.
  7. Persistência via `transactionRepository.save(entity)`.
  8. Mapeamento entity → DTO de saída (`PagamentoResponseDTO`).
  9. Retorno HTTP 201.

### 4.2. Busca por ID (`GET /pagamentos/{id}`)

- Busca `transactionRepository.findById(id)`
- Se não encontrar:
  - Lança `PaymentNotFoundException`
  - HTTP 404
- Se encontrar:
  - Entity → DTO via `TransactionMapper.toPaymentResponse`
  - HTTP 200

### 4.3. Listagem (`GET /pagamentos/listAllPayments`)

- `transactionRepository.findAll()`
- Cada entity é convertido em `PagamentoResponseDTO`
- Retorna `List<PagamentoResponseDTO>` com HTTP 200.

### 4.4. Estorno (`POST /pagamentos/{id}/estorno`)

Implementação em `PaymentServiceImpl.cancelPayment`:

- `findById(id).orElseThrow(() -> new PaymentNotFoundException(id))`
  - Se não existir → HTTP 404.
- Se status atual for `CANCELADO`:
  - Regra idempotente → retorna o próprio registro sem alterar.
- Senão:
  - Atualiza status para `CANCELADO`
  - Persiste com `save`
  - Retorna DTO com status `CANCELADO`.

---

## 5. Modelos de Dados (DTOs e Enums)

### 5.1. DTO de Entrada – `PagamentoRequestDTO`

Estrutura geral:

```json
{
  "transacao": {
    "id": "1000000000001",
    "cartao": "4111111111111111",
    "descricao": {
      "valor": 500.50,
      "dataHora": "01/05/2021 18:30:00",
      "estabelecimento": "PetShop Mundo cão"
    },
    "formaPagamento": {
      "tipo": "AVISTA",
      "parcelas": 1
    }
  }
}
```

Campos principais (via `TransacaoRequestDTO`, `DescricaoRequestDTO`, `FormaPagamentoDTO`):

- `transacao.id` (String)  
  - Obrigatório  
  - Numérico  
  - Único por base  
- `transacao.cartao` (String)  
  - Obrigatório  
- `descricao.valor` (BigDecimal)  
  - Obrigatório  
  - Maior que zero  
- `descricao.dataHora` (String)  
  - Formato `dd/MM/yyyy HH:mm:ss`  
  - Ex.: `"01/05/2021 18:30:00"`  
- `descricao.estabelecimento` (String)  
  - Obrigatório  
- `formaPagamento.tipo` (String)  
  - `"AVISTA"`, `"PARCELADO LOJA"`, `"PARCELADO EMISSOR"`  
- `formaPagamento.parcelas` (String)  
  - Obrigatório

### 5.2. DTO de Saída – `PagamentoResponseDTO`

Estrutura geral:

```json
{
  "transacao": {
    "id": "1000000000001",
    "cartao": "4111111111111111",
    "descricao": {
      "valor": 500.5,
      "dataHora": "01/05/2021 18:30:00",
      "estabelecimento": "PetShop Mundo cão",
      "nsu": "0000000001",
      "codigoAutorizacao": "a6aeca84e",
      "status": "AUTORIZADO"
    },
    "formaPagamento": {
      "tipo": "AVISTA",
      "parcelas": 1
    }
  }
}
```

Destaques:

- `descricao.nsu`  
  - String com 10 dígitos  
  - Ex.: `"0000000001"`  
- `descricao.codigoAutorizacao`  
  - String com 9 caracteres (gerado via UUID)  
- `descricao.status`  
  - Enum `StatusTransacao` em forma de texto (`"AUTORIZADO"`, `"NEGADO"`, `"CANCELADO"`).  

### 5.3. Enum `TipoFormaPagamento`

```java
public enum TipoFormaPagamento {

    AVISTA(1, "AVISTA"),
    PARCELADO_LOJA(2, "PARCELADO LOJA"),
    PARCELADO_EMISSOR(3, "PARCELADO EMISSOR");

    @JsonValue
    public String getDescricao() { ... }

    @JsonCreator
    public static TipoFormaPagamento fromValue(String value) { ... }

    public static TipoFormaPagamento fromId(Integer codigo) { ... }
}
```

### 5.4. Enum `StatusTransacao`

```java
public enum StatusTransacao {

    AUTORIZADO(1, "AUTORIZADO"),
    NEGADO(2, "NEGADO"),
    CANCELADO(3, "CANCELADO");

    @JsonValue
    public String getDescricao() { ... }

    @JsonCreator
    public static StatusTransacao fromValue(String value) { ... }

    public static StatusTransacao fromId(Integer codigo) { ... }
}
```

---

## 6. Regras Específicas: Parcelas x Tipo de Pagamento

Em `TransactionMapperImpl`:

```java
private Integer normalizePayment(String installmentsQtd, Integer paymentType) {
	Integer installmentsQtdInt = Integer.valueOf(installmentsQtd);
	Integer avistaCode = TipoFormaPagamento.AVISTA.getCodigo();
	if (installmentsQtdInt > 1 && avistaCode.equals(paymentType)) {
        throw new InvalidInstallmentsForPaymentTypeException();
    }
	return paymentType;
}
```

- Condição de disparo:
  - `paymentType` = código de `AVISTA`
  - `installmentsQtd > 1`
- Ação:
  - Lança `InvalidInstallmentsForPaymentTypeException`
  - Tratada no `GlobalExceptionHandler`:
    - HTTP 400  
    - Mensagem:  
      `"Installments quantity must be lower than 2 when payment type is AVISTA"`

---

## 7. Banco de Dados, Flyway e NSU

### 7.1. Banco

- Banco: **PostgreSQL**
- Database esperado: `toolschallenge_db`
- Usuário padrão de exemplo: `postgres`
- Senha padrão de exemplo: `postgres`

### 7.2. Sequence de NSU

- A aplicação espera uma sequence para gerar o NSU (`getNextNsu()`).

### 7.3. Script de inicialização

Caso não seja criada a `seq_nsu` automaticamente via migração, ou se você criar o banco na mão, use:

```text
src/main/resources/db/migration/init.sql
```

Esse script deve:

- Criar as tabelas necessárias  
- Criar a sequence usada para NSU  

---

## 8. Configuração de `application.properties` e Profiles

### 8.1. Variáveis de ambiente

A aplicação não deixa valores de conexão ao banco hardcoded no `application.properties`.  
Em vez disso, utiliza variáveis de ambiente (exemplo):

```properties
spring.datasource.url=${APP_DB_URL}
spring.datasource.username=${APP_DB_USERNAME}
spring.datasource.password=${APP_DB_PASSWORD}
```

### 8.2. Profiles (`application-dev.properties`)

Você pode ter um `application-dev.properties` com configurações específicas de desenvolvimento.  

```bash
export SPRING_PROFILES_ACTIVE=dev
```

---

## 9. Subindo o Ambiente (sem Docker)

### 9.1. Pré-requisitos

- Java 21 instalado  
- PostgreSQL instalado e rodando

### 9.2. Passo a passo

1. Criar o banco(caso não esteja criado):

   toolschallenge_db

2. Exportar variáveis de ambiente:

   ```
   $env:APP_DB_NAME="toolschallenge_db"
   $env:APP_DB_URL="jdbc:postgresql://localhost:5432/toolschallenge_db"
   $env:APP_DB_USERNAME="postgres"
   $env:APP_DB_PASSWORD="postgres"
   ```

3. Rodar a aplicação:
	
   ```
   ./gradlew bootRun
   ```

4. Acessar a API:

   - `http://localhost:8080/actuator/health`
   - `http://localhost:8080/pagamentos/listAllPayments`

---

## 10. Subindo o Ambiente com Docker / Docker Compose

### 10.1. Subir ambiente

```bash
docker compose build toolschallenge
docker compose down -v(APENAS SE QUISER DELETAR A IMAGEM DA API/BANCO)
docker compose up -d
```

---

## 11. Endpoints da API

### 11.1. Criar pagamento

- Método: **POST**  
- URL: `/pagamentos`  
- Corpo: `PagamentoRequestDTO`  
- Response: `PagamentoResponseDTO`  
- Status:
  - `201 CREATED` (sucesso)
  - `400 BAD REQUEST` (validação / JSON inválido)
  - `409 CONFLICT` (ID de transação duplicado)

### 11.2. Listar pagamentos

- Método: **GET**  
- URL: `/pagamentos/listAllPayments`  
- Response: `List<PagamentoResponseDTO>`  
- Status:
  - `200 OK`

### 11.3. Buscar por ID

- Método: **GET**  
- URL: `/pagamentos/{id}`  
- Path param: `id`  
- Status:
  - `200 OK`
  - `404 NOT FOUND` (`PaymentNotFoundException`)

### 11.4. Estornar pagamento

- Método: **POST**  
- URL: `/pagamentos/{id}/estorno`  
- Path param: `id`  
- Regras:
  - Se transação não existe → `404`
  - Se já estiver `CANCELADO` → operação idempotente, retorna o mesmo registro
  - Se estiver `AUTORIZADO` → muda para `CANCELADO` e salva  
- Status:
  - `200 OK`

---

## 12. Tratamento de Erros – Estrutura Padrão

A maioria das respostas de erro da API seguem o formato:

```json
{
  "timestamp": "2025-12-02T03:10:00Z",
  "message": "Texto descritivo do erro",
  "path": "/pagamentos/..."
}
```

Casos específicos:

- **Validação de DTO (`MethodArgumentNotValidException`)**
  - Adiciona `fieldErrors`:

    ```json
    {
      "timestamp": "...",
      "message": "Validation failed for one or more fields.",
      "path": "/pagamentos",
      "fieldErrors": [
        {
          "field": "transacao.id",
          "message": "must not be blank"
        }
      ]
    }
    ```

- **`ConstraintViolationException`**
  - Mensagem: `"Constraint violation for one or more parameters."`  
  - Também inclui `fieldErrors`.

- **`HttpMessageNotReadableException`**
  - Quando o problema é um tipo de pagamento inválido:
    ```text
    Invalid payment method type: XYZ Allowed values: AVISTA, PARCELADO LOJA, PARCELADO EMISSOR.
    ```

- **`MissingServletRequestParameterException`**
  - Mensagem:
    ```text
    Required request parameter 'id' of type String is missing
    ```

- **`MethodArgumentTypeMismatchException`**
  - Se o tipo esperado for conhecido:
    ```text
    Parameter 'id' has invalid value 'abc'. Expected type: Integer
    ```
  - Se não houver tipo requerido (`requiredClass == null`):
    - Usa `"unknown"` como tipo esperado.

- **`DuplicateTransactionIdException`**
  - HTTP 409  
  - Mensagem:
    ```text
    Transaction with id '<id>' already exists
    ```

- **`PaymentNotFoundException`**
  - HTTP 404  
  - Mensagem:
    ```text
    Transaction not found for id: <id>
    ```

- **`InvalidInstallmentsForPaymentTypeException`**
  - HTTP 400  
  - Mensagem:
    ```text
    Installments quantity must be lower than 2 when payment type is AVISTA
    ```

- **`Exception` genérica**
  - HTTP 500  
  - Mensagem fixa:
    ```text
    An unexpected error occurred. Please contact support if the problem persists.
    ```
---