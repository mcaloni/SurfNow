# SurfNow 🏄

Aplicativo mobile para surfers que exibe as condições de surf em praias brasileiras em tempo real, ordenando as praias mais próximas do usuário e pontuando as condições do mar.

---

## Funcionalidades

- **Praias próximas**: detecta a localização do usuário via GPS e ordena as praias pela distância
- **Condições em tempo real**: altura de onda, período, velocidade e direção do vento, direção e período do swell
- **Score de condições**: cada praia recebe uma nota de 0 a 10 baseada nas condições ideais para aquele spot
- **Múltiplas fontes de previsão**: Open-Meteo (gratuito) como base, com suporte a Windguru Pro para maior precisão
- **12 praias brasileiras** cadastradas do Sul ao Sudeste

---

## Tecnologias

### Mobile
- [React Native](https://reactnative.dev/) + [Expo SDK 54](https://expo.dev/)
- [Expo Router 6](https://expo.github.io/router/) — navegação baseada em arquivos
- [TanStack Query](https://tanstack.com/query) — gerenciamento de dados assíncronos
- [expo-location](https://docs.expo.dev/versions/latest/sdk/location/) — GPS

### Backend
- [Spring Boot 3.2](https://spring.io/projects/spring-boot) (Java 21)
- [PostgreSQL 16](https://www.postgresql.org/) — banco de dados
- [Flyway](https://flywaydb.org/) — migrações de banco
- [Open-Meteo](https://open-meteo.com/) — API gratuita de previsão (Marine + Forecast)
- [Windguru Pro](https://www.windguru.cz/) — fonte opcional de previsão (requer conta Pro + senha API)

### Infraestrutura
- [Docker Compose](https://docs.docker.com/compose/) — orquestração local

---

## Pré-requisitos

- [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- [Node.js 18+](https://nodejs.org/)
- [Expo Go](https://expo.dev/go) instalado no celular (iOS ou Android)

---

## Como rodar localmente

### 1. Clone o repositório

```bash
git clone https://github.com/mcaloni/SurfNow.git
cd SurfNow
```

### 2. Configure as variáveis de ambiente

Crie um arquivo `.env` na raiz do projeto:

```env
# Opcional: Windguru Pro (requer conta paga + senha secundária de API)
WINDGURU_USERNAME=seu@email.com
WINDGURU_PASSWORD=sua_senha_api_secundaria
```

> O arquivo `.env` está no `.gitignore` e nunca será commitado.

### 3. Suba o backend

```bash
docker compose up --build -d
```

Isso inicia:
- **PostgreSQL** na porta `5432`
- **Backend Spring Boot** na porta `8080`

### 4. Configure o IP da máquina no mobile

Edite [mobile/app.json](mobile/app.json) e substitua o IP em `extra.API_BASE_URL` pelo IP local da sua máquina:

```json
"extra": {
  "API_BASE_URL": "http://SEU_IP_LOCAL:8080"
}
```

> Para descobrir seu IP: `ipconfig getifaddr en0` (macOS) ou `hostname -I` (Linux)

### 5. Inicie o Metro (bundler)

```bash
cd mobile
npm install
npm start -- --clear
```

### 6. Abra no celular

Escaneie o QR code exibido no terminal com o app **Expo Go**.

---

## Estrutura do projeto

```
SurfNow/
├── backend/                        # Spring Boot API
│   └── src/main/java/com/surfnow/
│       ├── api/                    # Controllers REST
│       ├── application/beach/      # Casos de uso
│       ├── domain/                 # Entidades e regras de negócio
│       └── infrastructure/
│           └── external/
│               ├── OpenMeteoClient.java       # Fonte principal (gratuita)
│               └── windguru/
│                   ├── WindguruForecastPort.java    # Interface da fonte Windguru
│                   ├── WindguruProApiClient.java    # Windguru Pro (autenticado)
│                   └── WindguruScrapingClient.java  # Fallback scraping
├── mobile/                         # React Native + Expo
│   └── src/
│       ├── app/                    # Telas (Expo Router)
│       │   └── (tabs)/             # Praias, Mapa, Feed, Alertas
│       ├── features/beaches/       # Componente BeachCard
│       └── services/               # Chamadas à API
├── docker-compose.yml
└── .env                            # Credenciais locais (não commitado)
```

---

## Fontes de previsão

| Fonte | Tipo | Dados | Ativação |
|-------|------|-------|----------|
| Open-Meteo Marine | Gratuita | Altura de onda, período, swell | Sempre ativo (fallback) |
| Open-Meteo Forecast | Gratuita | Vento (velocidade e direção) | Sempre ativo |
| Windguru Pro | Paga | Vento, swell (alta precisão) | Quando `WINDGURU_USERNAME` definido |

Quando o Windguru Pro está ativo, os dados de vento e swell vêm do Windguru, e a altura/período da onda vêm do Open-Meteo (não fornecido pelo Windguru).

### Configurando a senha API do Windguru Pro

1. Acesse [windguru.cz](https://www.windguru.cz) e faça login
2. Vá em **My account** → seção **API password**
3. Crie uma senha secundária específica para API
4. Coloque no `.env` como `WINDGURU_PASSWORD`

---

## Como o score é calculado

Cada praia tem configurações de condições ideais no banco de dados:
- Altura mínima e máxima de onda ideal
- Direção de vento offshore
- Melhor direção de swell

O score (0–10) é calculado comparando as condições atuais com o ideal de cada praia.

| Score | Label |
|-------|-------|
| 8–10 | Excelente |
| 6–8 | Bom |
| 4–6 | Regular |
| 0–4 | Ruim |

---

## Praias cadastradas

| Praia | Estado |
|-------|--------|
| Maresias | SP |
| Barra do Una | SP |
| Boiçucanga | SP |
| Guarujá | SP |
| Ilhabela | SP |
| Saquarema | RJ |
| Ipanema | RJ |
| Barra da Tijuca | RJ |
| Itacoatiara | RJ |
| Praia do Rosa | SC |
| Torres | RS |

---

## Parando o projeto

```bash
# Para o backend e o banco
docker compose down

# Para o Metro (Ctrl+C no terminal onde está rodando)
```
