
# Authorizer

## Descrição

O **Authorizer** é um projeto desenvolvido com Spring Boot, Java 21 e PostgreSQL. O projeto oferece uma simples API para autorização de transações de cartão de crédito.


### [Documentação técnica](./docs/design-docs.md)

## Requisitos

Antes de começar, verifique se você tem os seguintes requisitos instalados:

- Docker
- Docker Compose

## Executando o Projeto

### Docker Compose

Para rodar o projeto usando Docker Compose, siga estas etapas:

1. Navegue até a raiz do projeto:

   ```sh
   cd /authorizer

2. Certifique-se de que o arquivo docker-compose.yml está na raiz do projeto e está corretamente configurado.


3. Execute o comando Docker Compose para iniciar os contêineres:
   ```sh
   docker compose up -d
   ```

    Isso iniciará o serviço do Spring Boot e o banco de dados PostgreSQL definidos no docker-compose.yml.



4. A API estará disponível em http://localhost:8080


## Testando a API

```sh
curl -X POST http://localhost:8080/authorize \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: $(uuidgen)" \
  -d '{
    "account": "1",
    "totalAmount": 35.50,
    "mcc": "5411",
    "merchant": "UBER EATS                   SAO PAULO BR"
  }'

```

Alternativa: Arquivo request.http.

Caso prefira, você também pode usar o arquivo request.http localizado na raiz do projeto para fazer requisições de teste. Este arquivo já está configurado com exemplos de requisições que você pode executar diretamente em ferramentas que suportam arquivos .http, como o Visual Studio Code com a extensão REST Client ou IntelliJ.
