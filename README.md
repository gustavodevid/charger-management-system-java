# Charge Management System

Sistema distribuído de gerenciamento de cobranças com integração ao gateway de pagamento ASAAS, construído com **Jakarta EE** e orquestrado com **Docker Swarm**.

## Tecnologias

| Categoria | Tecnologia |
|----------|------------|
| Linguagem | Java 17 |
| Framework | Jakarta EE (Servlet 6.0, JAX-WS 4.0) |
| Servidor | Apache Tomcat 10.1 |
| Banco de Dados | PostgreSQL 15 |
| Acesso a Dados | JDBC puro |
| Connection Pool | HikariCP |
| Migrações | Flyway |
| Serviços Web | JAX-WS (SOAP) |
| Cliente HTTP | Apache HttpClient 5 |
| Email | Jakarta Mail (Angus Mail) |
| Orquestração | Docker Swarm |
| Contêiner | Docker |

## Como Executar

### Pré-requisitos
- Docker instalado e rodando
- PowerShell (Windows) ou Terminal (Linux/macOS)

### Passo a Passo

#### 1. Construir as Imagens Docker

**Windows (PowerShell):**
```powershell
.\scripts\build-all.ps1
```

**Linux/macOS:**
```bash
chmod +x scripts/*.sh
./scripts/build-all.sh
```

#### 2. Inicializar Docker Swarm

**Windows (PowerShell):**
```powershell
.\scripts\swarm-init.ps1
```

**Linux/macOS:**
```bash
./scripts/swarm-init.sh
```

#### 3. Deploy no Docker Swarm

**Windows (PowerShell):**
```powershell
.\scripts\swarm-deploy.ps1
```

**Linux/macOS:**
```bash
./scripts/swarm-deploy.sh
```

#### Alternativa: Modo Standalone (sem Swarm)

**Windows (PowerShell):**
```powershell
.\scripts\start-all.ps1
```

**Linux/macOS:**
```bash
./scripts/start-all.sh
```

#### Verificar os WSDLs

- **Charge Manager**: http://localhost:8080/ws/customer?wsdl
- **Charge Proxy**: http://localhost:8082/ws/charge?wsdl

#### Health Checks

- **Charge Manager**: http://localhost:8080/health
- **Charge Proxy**: http://localhost:8082/health

#### Testar o Serviço SOAP

**Health Check:**
```bash
curl -X POST "http://localhost:8080/ws/customer" \
  -H "Content-Type: text/xml" \
  -d '<?xml version="1.0"?><soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:soap="http://chargemanager.dac.com/soap"><soapenv:Body><soap:healthCheck/></soapenv:Body></soapenv:Envelope>'
```

**Criar Cliente:**
```bash
curl -X POST "http://localhost:8080/ws/customer" \
  -H "Content-Type: text/xml" \
  -d '<?xml version="1.0"?><soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:soap="http://chargemanager.dac.com/soap"><soapenv:Body><soap:createCustomer><CustomerRequest><name>João Silva</name><email>joao@example.com</email><cpfCnpj>12345678901</cpfCnpj><phone>11999999999</phone></CustomerRequest></soap:createCustomer></soapenv:Body></soapenv:Envelope>'
```

#### Parar o Sistema

**Docker Swarm:**
```bash
# Linux/macOS
./scripts/swarm-remove.sh

# Windows
.\scripts\swarm-remove.ps1
```

**Modo Standalone:**
```bash
./scripts/stop-all.sh
```

## Arquitetura

```
┌─────────────────────────────────────────────────────────────────────┐
│                     Docker Swarm Overlay Network                     │
│  ┌──────────────────┐    ┌──────────────────┐    ┌───────────────┐  │
│  │  Charge Manager  │    │   Charge Proxy   │    │  PostgreSQL   │  │
│  │  (Tomcat:8080)   │◄──►│  (Tomcat:8082)   │    │  (Port 5432)  │  │
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
│   │   ├── api/                 # Camada API (SOAP, Servlets)
│   │   │   ├── soap/            # Endpoints SOAP (JAX-WS)
│   │   │   └── servlet/         # Health check servlet
│   │   ├── business/            # Serviços, DTOs, Exceções
│   │   └── infra/               # Repositórios, Entidades, Config
│   │       ├── config/          # DatabaseConfig, ServiceLocator
│   │       ├── entity/          # Entidades JPA
│   │       └── repository/      # Repositórios JDBC
│   ├── src/main/webapp/WEB-INF/ # Configuração web
│   │   ├── web.xml              # Configuração Servlet
│   │   └── sun-jaxws.xml        # Configuração JAX-WS
│   ├── src/main/resources/
│   │   ├── application.properties
│   │   ├── logback.xml
│   │   └── db/migration/        # Migrações Flyway
│   ├── Dockerfile
│   └── pom.xml
├── charge-proxy/                # Serviço proxy SOAP
│   ├── src/main/java/com/dac/chargeproxy/
│   │   ├── soap/                # Endpoint JAX-WS SOAP
│   │   ├── client/              # Cliente ASAAS (HttpClient)
│   │   ├── config/              # Configuração e ServiceLocator
│   │   └── servlet/             # Health check servlet
│   ├── src/main/webapp/WEB-INF/
│   │   ├── web.xml
│   │   └── sun-jaxws.xml
│   ├── Dockerfile
│   └── pom.xml
├── scripts/                     # Scripts de orquestração Docker
├── docker-stack.yml             # Arquivo de stack do Docker Swarm
├── pom.xml                      # POM pai
└── README.md
```

## Endpoints SOAP

### Charge Manager - Customer Service

**WSDL**: http://localhost:8080/ws/customer?wsdl

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

| Contêiner | Porta | Descrição |
|-----------|-------|-----------|
| charge-db | 5432 | Banco de dados PostgreSQL |
| charge-manager | 8080 | Tomcat (SOAP + Health) |
| charge-proxy | 8082 | Tomcat (SOAP + Health) |

## Docker Swarm

O projeto utiliza **Docker Swarm** para orquestração dos containers:

- **Overlay Network**: Rede isolada para comunicação entre serviços
- **Service Discovery**: Serviços se descobrem automaticamente pelo nome
- **Restart Policies**: Reinicialização automática em caso de falhas
- **Health Checks**: Verificação de saúde dos containers
- **Rolling Updates**: Atualizações sem downtime

### Comandos Úteis do Swarm

```bash
# Ver serviços
docker stack services charge-system

# Ver tarefas (containers)
docker stack ps charge-system

# Escalar serviço
docker service scale charge-system_charge-manager=3

# Ver logs em tempo real
docker service logs -f charge-system_charge-manager

# Inspecionar serviço
docker service inspect charge-system_charge-manager
```

## Iterações de Entrega

### Iteração 1 (Atual)
- [x] Todos os módulos ativados via Docker
- [x] Rota funcional passando por todas as camadas até o banco de dados
- [x] CRUD de clientes com arquitetura de 3 camadas
- [x] Endpoints SOAP (JAX-WS)
- [x] Docker Swarm para orquestração
- [x] Jakarta EE puro (sem Spring Boot)
