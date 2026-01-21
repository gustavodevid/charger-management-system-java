# Charge Management System

Sistema distribuído de gerenciamento de cobranças com integração ao gateway de pagamento ASAAS, construído com **Spring Framework** e orquestrado com **Docker Swarm**.

## Tecnologias

| Categoria | Tecnologia |
|----------|------------|
| Linguagem | Java 17 |
| Framework | Spring Framework 6.1 |
| Servidor | Apache Tomcat 10.1 |
| Banco de Dados | PostgreSQL 15 |
| Acesso a Dados | JDBC + Spring JDBC |
| Connection Pool | HikariCP |
| Migrações | Flyway |
| Serviços Web | JAX-WS (SOAP) + Spring Integration |
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

- **Charge Manager (Customer)**: http://localhost:8080/ws/customer?wsdl
- **Charge Manager (Charge)**: http://localhost:8080/ws/charge?wsdl
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
