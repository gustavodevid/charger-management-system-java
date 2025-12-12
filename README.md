# Charge Management System

Sistema distribuído de gerenciamento de cobranças com integração ao gateway de pagamento ASAAS, construído com Spring Boot e containerizado com Docker.

## Como Executar

### Pré-requisitos
- Docker instalado e rodando
- PowerShell (Windows) ou Terminal (Linux/macOS)

### Passo a Passo

#### Construir as Imagens Docker

**Windows (PowerShell):**
```powershell
.\scripts\build-all.ps1
```

**Linux/macOS:**
```bash
chmod +x scripts/*.sh
./scripts/build-all.sh
```

#### Iniciar o Sistema

**Windows (PowerShell):**
```powershell
.\scripts\start-all.ps1
```

**Linux/macOS:**
```bash
./scripts/start-all.sh
```

#### Verificar os WSDLs

- **Charge Manager**: http://localhost:8081/ws/customer?wsdl
- **Charge Proxy**: http://localhost:8082/ws/charge?wsdl

#### Testar o Serviço SOAP

**Health Check:**
```bash
curl -X POST "http://localhost:8081/ws/customer" \
  -H "Content-Type: text/xml" \
  -d '<?xml version="1.0"?><soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:soap="http://chargemanager.dac.com/soap"><soapenv:Body><soap:healthCheck/></soapenv:Body></soapenv:Envelope>'
```

**Criar Cliente:**
```bash
curl -X POST "http://localhost:8081/ws/customer" \
  -H "Content-Type: text/xml" \
  -d '<?xml version="1.0"?><soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:soap="http://chargemanager.dac.com/soap"><soapenv:Body><soap:createCustomer><CustomerRequest><name>João Silva</name><email>joao@example.com</email><cpfCnpj>12345678901</cpfCnpj><phone>11999999999</phone></CustomerRequest></soap:createCustomer></soapenv:Body></soapenv:Envelope>'
```

#### Parar o Sistema

**Windows (PowerShell):**
```powershell
.\scripts\stop-all.ps1

# Parar e remover tudo (incluindo dados)
.\scripts\stop-all.ps1 -Clean
```

## Arquitetura

```
┌─────────────────────────────────────────────────────────────────────┐
│                        Docker Network                                │
│  ┌──────────────────┐    ┌──────────────────┐    ┌───────────────┐  │
│  │  Charge Manager  │    │   Charge Proxy   │    │  PostgreSQL   │  │
│  │  (Ports 8080/81) │◄──►│ (Ports 8082/83)  │    │  (Port 5432)  │  │
│  │                  │    │                  │    │               │  │
│  │  ┌───────────┐   │    │  ┌────────────┐  │    │  ┌─────────┐  │  │
│  │  │   SOAP    │   │    │  │    SOAP    │  │    │  │   DB    │  │  │
│  │  │ Endpoint  │   │    │  │  Endpoint  │  │    │  └─────────┘  │  │
│  │  ├───────────┤   │    │  ├────────────┤  │    │               │  │
│  │  │ Business  │   │    │  │   ASAAS    │  │    │               │  │
│  │  ├───────────┤   │    │  │   Client   │  │    │               │  │
│  │  │   Infra   │───┼────┼──►           │  │    │               │  │
│  │  └───────────┘   │    │  └────────────┘  │    │               │  │
│  └──────────────────┘    └──────────────────┘    └───────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
```

## Estrutura do Projeto

```
charger-management-system-java/
├── charge-manager/              # Serviço principal (arquitetura 3 camadas)
│   ├── src/main/java/com/dac/chargemanager/
│   │   ├── api/                 # Camada API (SOAP)
│   │   │   ├── soap/            # Endpoints SOAP
│   │   │   └── config/          # Configurações
│   │   ├── business/            # Serviços, DTOs, Exceções
│   │   └── infra/               # Repositórios, Entidades
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── db/migration/        # Migrações Flyway
│   ├── Dockerfile
│   └── pom.xml
├── charge-proxy/                # Serviço proxy SOAP
│   ├── src/main/java/com/dac/chargeproxy/
│   │   ├── soap/                # Endpoint JAX-WS SOAP
│   │   ├── client/              # Cliente ASAAS
│   │   └── controller/          # Health check REST
│   ├── Dockerfile
│   └── pom.xml
├── scripts/                     # Scripts de orquestração Docker
│   ├── build-all.sh / .ps1
│   ├── start-all.sh / .ps1
│   ├── stop-all.sh / .ps1
│   └── logs.sh
├── pom.xml                      # POM pai
└── README.md
```

## Tecnologias

| Categoria | Tecnologia |
|----------|------------|
| Linguagem | Java 17 |
| Framework | Spring Boot 3.2 |
| Banco de Dados | PostgreSQL 15 |
| Acesso a Dados | Spring JDBC |
| Migrações | Flyway |
| Serviços Web | JAX-WS (SOAP) |
| Cliente HTTP | OpenFeign |
| Contêiner | Docker |

## Endpoints SOAP

### Charge Manager - Customer Service

**WSDL**: http://localhost:8081/ws/customer?wsdl

| Operação | Descrição |
|----------|-----------|
| `createCustomer` | Criar um novo cliente |
| `getCustomer` | Obter cliente por ID |
| `getAllCustomers` | Listar todos os clientes |
| `updateCustomer` | Atualizar cliente |
| `deleteCustomer` | Excluir cliente |
| `healthCheck` | Verificar saúde do serviço |

### Charge Proxy - Charge Service

**WSDL**: http://localhost:8082/ws/charge?wsdl

| Operação | Descrição |
|----------|-----------|
| `createCharge` | Criar uma cobrança (PIX, BOLETO, CREDIT_CARD) |
| `getCharge` | Obter cobrança por ID |
| `cancelCharge` | Cancelar cobrança |
| `healthCheck` | Verificar saúde do serviço |

## Exemplos de Requisições SOAP

### Criar Cliente

```xml
<?xml version="1.0" encoding="UTF-8"?>
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" 
                  xmlns:soap="http://chargemanager.dac.com/soap">
   <soapenv:Header/>
   <soapenv:Body>
      <soap:createCustomer>
         <CustomerRequest>
            <name>João Silva</name>
            <email>joao@example.com</email>
            <cpfCnpj>12345678901</cpfCnpj>
            <phone>11999999999</phone>
         </CustomerRequest>
      </soap:createCustomer>
   </soapenv:Body>
</soapenv:Envelope>
```

### Obter Cliente por ID

```xml
<?xml version="1.0" encoding="UTF-8"?>
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" 
                  xmlns:soap="http://chargemanager.dac.com/soap">
   <soapenv:Header/>
   <soapenv:Body>
      <soap:getCustomer>
         <customerId>1</customerId>
      </soap:getCustomer>
   </soapenv:Body>
</soapenv:Envelope>
```

### Listar Todos os Clientes

```xml
<?xml version="1.0" encoding="UTF-8"?>
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" 
                  xmlns:soap="http://chargemanager.dac.com/soap">
   <soapenv:Header/>
   <soapenv:Body>
      <soap:getAllCustomers/>
   </soapenv:Body>
</soapenv:Envelope>
```

### Health Check

```xml
<?xml version="1.0" encoding="UTF-8"?>
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" 
                  xmlns:soap="http://chargemanager.dac.com/soap">
   <soapenv:Header/>
   <soapenv:Body>
      <soap:healthCheck/>
   </soapenv:Body>
</soapenv:Envelope>
```

## Contêineres Docker

| Contêiner | Porta(s) | Descrição |
|-----------|----------|-----------|
| charge-db | 5432 | Banco de dados PostgreSQL |
| charge-manager | 8080, 8081 | Serviço SOAP (8081) + Actuator (8080) |
| charge-proxy | 8082, 8083 | Serviço SOAP (8082) + Actuator (8083) |

## Visualizar Logs

```bash
# Ver logs de todos os serviços
docker logs charge-manager
docker logs charge-proxy
docker logs charge-db

# Ou usar o script (Linux/macOS)
./scripts/logs.sh all
```

## Iterações de Entrega

### Iteração 1 (Atual)
- [x] Todos os módulos ativados via Docker
- [x] Rota funcional passando por todas as camadas até o banco de dados
- [x] CRUD de clientes com arquitetura de 3 camadas
- [x] Endpoints SOAP (JAX-WS)
