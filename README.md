# CDQ Assistant

AI-powered chatbot REST API built with Spring Boot and LangChain4j. The assistant uses tool-augmented generation to answer questions about countries, CDQ knowledge base (via RAG), and current weather (via MCP).

## Features

- **Country Tool** — fetches country details (capital, population, languages, currencies) from REST Countries API
- **CDQ Knowledge Tool** — semantic search over a CDQ knowledge base using RAG with PgVector embeddings
- **Weather Tool** — current weather data via MCP (Model Context Protocol) integration
- Tool-first resolution — the agent always calls tools before falling back to LLM knowledge

## Prerequisites

- [Docker](https://docs.docker.com/get-docker/) and Docker Compose
- Weather API key from [WeatherAPI](https://www.weatherapi.com/)

## Quick Start

### 1. Clone the repository

```bash
git clone --recurse-submodules https://github.com/mikolajkozlowskiii/cdq-assistant.git
cd assistant
```

> The `--recurse-submodules` flag is required to fetch the MCP weather tool dependency.

### 2. Create `.env` file

```properties
DB_USER=postgres
DB_PASSWORD=postgres
DB_NAME=assistant_db
WEATHER_API_KEY=<your-weatherapi-key>
WEATHER_API_URL=http://api.weatherapi.com/v1/current.json
```

### 3. Run with Docker Compose

```bash
docker compose up --build
```

By default, the app uses **Ollama** (`qwen3:4b`) as the LLM — no external API key needed. The first startup will take a few minutes to download the embedding and LLM models.

The API will be available at `http://localhost:8080`.

## Using OpenAI Instead of Ollama

To use OpenAI as the chat model, add `OPENAI_API_KEY` to your `.env` file:

```properties
OPENAI_API_KEY=<your-openai-api-key>
```

Then start the app with the `openai` profile:

```bash
SPRING_PROFILES_ACTIVE=openai docker compose up --build
```

This uses `gpt-4o` by default. The embedding model (Ollama) is still used for RAG regardless of the active profile.

## API Usage

### Chat endpoint

```
POST /api/v1/chat
Content-Type: application/json
```

#### Country information

Request:
```json
{
    "message": "What is the capital city of Germany?"
}
```

Response:
```json
{
    "answer": "The capital city of Germany is Berlin.",
    "timestamp": "2026-02-17T23:48:39.328051046Z",
    "usedSources": [
        {
            "name": "getCountryData",
            "detail": "{\"countryName\":\"Germany\"}"
        }
    ]
}
```

#### Weather

Request:
```json
{
    "message": "What is the temperature currently in Munich?"
}
```

Response:
```json
{
    "answer": "The current temperature in Munich is 1.4°C.",
    "timestamp": "2026-02-17T23:49:08.496450379Z",
    "usedSources": [
        {
            "name": "get-weather",
            "detail": "{\"city\":\"munich\"}"
        }
    ]
}
```

#### Multi-tool chaining

Request:
```json
{
    "message": "What is the temperature of the capital of Germany currently?"
}
```

Response:
```json
{
    "answer": "The current temperature in Berlin, the capital of Germany, is -1.7°C.",
    "timestamp": "2026-02-17T23:49:37.646820087Z",
    "usedSources": [
        {
            "name": "getCountryData",
            "detail": "{\"countryName\":\"Germany\"}"
        },
        {
            "name": "get-weather",
            "detail": "{\"city\":\"Berlin\"}"
        }
    ]
}
```

#### Detailed country lookup

Request:
```json
{
    "message": "What do you know about Berlin?"
}
```

Response:
```json
{
    "answer": "Berlin is the capital city of Germany. It is located in the northeastern part of the country. Germany, officially known as the Federal Republic of Germany, is situated in Western Europe and is bordered by countries such as Austria, Belgium, Czech Republic, Denmark, France, Luxembourg, Netherlands, Poland, and Switzerland. The official language is German, and the currency used is the Euro (€). Germany has a population of approximately 83.5 million people. \n\nFor more information, you can view Berlin on [Google Maps](https://goo.gl/maps/mD9FBMq1nvXUBrkv6).",
    "timestamp": "2026-02-17T23:49:58.047098847Z",
    "usedSources": [
        {
            "name": "getCountryData",
            "detail": "{\"countryName\":\"Germany\"}"
        }
    ]
}
```

#### CDQ knowledge base (RAG)

Request:
```json
{
    "message": "What includes CDQ integration?"
}
```

Response:
```json
{
    "answer": "CDQ integration includes:\n\n- Integration with major ERP systems, such as SAP.\n- Access via REST APIs.\n- It is part of the broader CDQ Cloud Platform, which offers additional data quality and governance services.",
    "timestamp": "2026-02-17T23:50:56.994539721Z",
    "usedSources": [
        {
            "name": "searchCdqKnowledge",
            "detail": "{\"query\":\"CDQ integration\"}"
        }
    ]
}
```

#### All tools combined

Request:
```json
{
    "message": "What is Anomaly detection in CDQ Fraud Guard? And what is current temperature of the capital of Thailand?"
}
```

Response:
```json
{
    "answer": "In CDQ Fraud Guard, anomaly detection identifies unusual changes in master data that may indicate fraudulent manipulation, such as sudden address changes, modified bank details, or altered ownership structures.\n\nThe current temperature in Bangkok, the capital of Thailand, is 26.3°C.",
    "timestamp": "2026-02-18T00:17:25.453829303Z",
    "usedSources": [
        {
            "name": "searchCdqKnowledge",
            "detail": "{\"query\": \"Anomaly detection in CDQ Fraud Guard\"}"
        },
        {
            "name": "getCountryData",
            "detail": "{\"countryName\": \"Thailand\"}"
        },
        {
            "name": "get-weather",
            "detail": "{\"city\":\"Bangkok\"}"
        }
    ]
}
```

## Configuration

All configuration is done via environment variables. See `application.yml` for defaults.

| Variable | Default | Description |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `ollama` (Docker) / `openai` (local) | Active Spring profile |
| `OPENAI_API_KEY` | — | OpenAI API key (required for `openai` profile) |
| `OPENAI_MODEL` | `gpt-4o` | OpenAI model name |
| `WEATHER_API_KEY` | — | WeatherAPI key |
| `WEATHER_API_URL` | `http://api.weatherapi.com/v1/current.json` | WeatherAPI endpoint |
| `OLLAMA_BASE_URL` | `http://localhost:11434` | Ollama server URL |

## Tech Stack

- Java 21, Spring Boot 4.0.2
- LangChain4j 1.11.0
- PostgreSQL with pgvector for RAG embeddings
- Ollama for local LLM and embedding models
- MCP (Model Context Protocol) for weather tool integration
