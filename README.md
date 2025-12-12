# Charge Management System

Sistema distribuído de gerenciamento de cobranças com integração ao gateway de pagamento ASAAS, construído com Spring Boot e orquestrado com **Docker Swarm**.

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

#### Verificar Status do Swarm

**Windows (PowerShell):**
```powershell
.\scripts\swarm-status.ps1
```

**Linux/macOS:**
```bash
./scripts/swarm-status.sh
```

#### Parar o Sistema

**Docker Swarm:**
```powershell
# Windows
.\scripts\swarm-remove.ps1

# Linux/macOS
./scripts/swarm-remove.sh

# Para também sair do Swarm:
.\scripts\swarm-remove.ps1 -Leave
./scripts/swarm-remove.sh --leave
```

**Modo Standalone:**
```powershell
.\scripts\stop-all.ps1 -Clean
```

## Arquitetura

```
┌─────────────────────────────────────────────────────────────────────┐
│                     Docker Swarm Overlay Network                     │
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
│   ├── build-all.sh / .ps1      # Construir imagens Docker
│   ├── swarm-init.sh / .ps1     # Inicializar Docker Swarm
│   ├── swarm-deploy.sh / .ps1   # Deploy no Swarm
│   ├── swarm-status.sh / .ps1   # Status do Swarm
│   ├── swarm-remove.sh / .ps1   # Remover stack do Swarm
│   ├── start-all.sh / .ps1      # Modo standalone (sem Swarm)
│   ├── stop-all.sh / .ps1       # Parar modo standalone
│   └── logs.sh
├── docker-stack.yml             # Arquivo de stack do Docker Swarm
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
| Orquestração | **Docker Swarm** |
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

**Docker Swarm:**
```bash
# Ver logs dos serviços
docker service logs charge-system_charge-manager
docker service logs charge-system_charge-proxy
docker service logs charge-system_charge-db

# Ver status dos serviços
docker stack services charge-system
docker stack ps charge-system
```

**Modo Standalone:**
```bash
docker logs charge-manager
docker logs charge-proxy
docker logs charge-db
```

## Docker Swarm

O projeto utiliza **Docker Swarm** para orquestração dos containers, oferecendo:

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
- [x] **Docker Swarm** para orquestração

