# PDF Chat Bot - Document Q&A System

A full-stack application combining **React + Vite** frontend with **Spring Boot** backend for intelligent document Q&A powered by Ollama LLM. Upload PDFs and chat about their content with an AI chatbot that remembers previous questions and context.

## 🎯 Features

- **📄 PDF Upload**: Upload PDF documents (max 1 MB) with automatic text extraction
- **🤖 AI-Powered Chat**: Ask questions about documents using local Ollama LLM
- **💬 Chatbot Memory**: Conversations remember previous messages and document context
- **⚡ Real-time WebSocket**: Live chat with instant message delivery using WebSocket/STOMP
- **🔌 Connection Status**: Visual indicator showing WebSocket connection state
- **🔐 Session Management**: Each document gets its own chat session
- **⚡ Single Port**: Frontend and backend run on **port 8080**
- **🐳 Docker Support**: Complete Docker Compose setup with Ollama, MongoDB, and Spring Boot
- **✅ Input Validation**: File type and size validation
- **🚨 Error Handling**: Centralized exception handling with detailed error responses
- **🔄 Auto-reconnect**: Automatic WebSocket reconnection on connection loss

## 🏗️ Project Structure

```
DocumentSummary/
├── frontend/                          # React + Vite UI
│   ├── src/
│   │   ├── components/
│   │   │   ├── DocumentUpload.jsx    # File upload component
│   │   │   └── ChatBot.jsx           # Chat interface
│   │   ├── api/
│   │   │   └── documentApi.js        # API calls
│   │   ├── App.jsx                   # Main app component
│   │   └── main.jsx                  # Entry point
│   ├── package.json
│   └── vite.config.js
├── src/main/java/com/docqa/          # Spring Boot application
│   ├── controller/
│   │   ├── DocumentController.java   # Upload endpoint
│   │   └── ChatController.java       # Chat endpoint
│   ├── service/
│   │   ├── DocumentService.java      # PDF processing
│   │   ├── ChatService.java          # Chat logic
│   │   └── OllamaService.java        # LLM integration
│   ├── model/                        # Data entities
│   ├── repository/                   # Database access
│   ├── dto/                          # Data transfer objects
│   └── DocumentSummaryApplication.java
├── docker-compose.yml                # Services orchestration
├── Dockerfile                        # Container build
├── pom.xml                          # Maven configuration
└── README.md
```

## 📋 API Endpoints

### 1. Upload Document
**POST** `/api/v1/documents/upload`

Upload a PDF and start a chat session.

**Request:**
```
Content-Type: multipart/form-data
file: <PDF file>
query: <optional initial question>
```

**Response (201 Created):**
```json
{
  "documentId": "507f1f77bcf86cd799439011",
  "sessionId": "sess-123456",
  "response": "Document loaded successfully! Ask me any questions about the document.",
  "documentName": "sample.pdf"
}
```

### 2. Send Chat Message
**POST** `/api/v1/chat/message`

Send a question about the document and get an AI response.

**Request:**
```json
{
  "sessionId": "sess-123456",
  "question": "What is the main topic?"
}
```

**Response (200 OK):**
```json
{
  "sessionId": "sess-123456",
  "documentId": "507f1f77bcf86cd799439011",
  "messages": [
    {
      "role": "user",
      "content": "What is the main topic?",
      "timestamp": "2025-12-13T10:30:00Z"
    },
    {
      "role": "assistant",
      "content": "The main topic is...",
      "timestamp": "2025-12-13T10:30:05Z"
    }
  ]
}
```

### 3. WebSocket Chat (Real-time)
**WebSocket Endpoint:** `ws://localhost:8080/ws`

Connect to WebSocket for live chat updates.

**Subscribe to:** `/topic/chat/{sessionId}`

**Send to:** `/app/chat/message`

**Message Format:**
```json
{
  "sessionId": "sess-123456",
  "question": "What is the main topic?"
}
```

**Received Messages:**
```json
{
  "role": "user" | "assistant" | "error",
  "content": "Message content"
}
```

**Features:**
- 🔴/🟢 Connection status indicator
- Real-time message delivery
- Automatic reconnection
- SockJS fallback for older browsers

## 🚀 Quick Start

### Prerequisites

- **Docker & Docker Compose** (easiest way)
- **OR**: Java 21, Maven 3.9+, Node.js 25+

### Option 1: Docker Compose (Recommended)

1. **Start all services:**
   ```bash
   docker-compose up -d
   ```

2. **Wait for services to initialize** (1-2 minutes):
   - Ollama downloads the LLM model (llama3.1:8b)
   - MongoDB initializes
   - Spring Boot starts and serves the frontend

3. **Open in browser:**
   ```
   http://localhost:8080
   ```

4. **Check service status:**
   ```bash
   docker-compose ps
   ```

### Option 2: Local Development

#### 1. Start Ollama
```bash
docker run -d -p 11434:11434 --name ollama ollama/ollama:latest
docker exec ollama ollama pull llama3.1:8b
```

#### 2. Start MongoDB
```bash
docker run -d -p 27017:27017 \
  -e MONGO_INITDB_ROOT_USERNAME=root \
  -e MONGO_INITDB_ROOT_PASSWORD=example \
  --name mongodb mongo:7.0
```

#### 3. Build and Run Spring Boot
```bash
mvn clean package
java -jar target/PDFChatBot.jar
```

#### 4. Open in browser
```
http://localhost:8080
```

## 💻 How to Use

1. **Upload PDF**: Click "Upload Document" and select a PDF file (max 1 MB)
2. **Chat**: Once uploaded, ask questions in the chat box
3. **History**: All previous messages and document context are remembered
4. **New Document**: Click "Upload New" to switch to a different PDF

## ⚙️ Configuration

### Backend (application.yml)
```yaml
spring:
  ai:
    ollama:
      base-url: http://localhost:11434
      chat:
        model: llama3.1:8b
  data:
    mongodb:
      uri: mongodb://root:example@localhost:27017/document-summary?authSource=admin

server:
  port: 8080

app:
  max-file-size: 1048576  # 1 MB
```

### Frontend (vite.config.js)
Frontend is built into the Spring Boot JAR and served from `/` route.

## 🔧 Technology Stack

**Backend:**
- Spring Boot 3.5.7 (Java 21)
- Spring WebSocket & STOMP (Real-time messaging)
- Spring Data MongoDB
- Spring AI (Ollama integration)
- Apache PDFBox (PDF extraction)
- Lombok

**Frontend:**
- React 18+
- Vite
- Axios (API calls)
- STOMP.js & SockJS (WebSocket client)

**Infrastructure:**
- Ollama (LLM)
- MongoDB (Database)
- Docker & Docker Compose

## 📝 Environment Variables

```bash

# Ollama Configuration
SPRING_AI_OLLAMA_BASE_URL=http://ollama:11434
SPRING_AI_OLLAMA_CHAT_MODEL=llama3.1:8b

# MongoDB Configuration
SPRING_DATA_MONGODB_URI=mongodb://root:example@localhost:27017/document-summary?authSource=admin

# Application
SERVER_PORT=8080
APP_MAX_FILE_SIZE=1048576
```

## 🧪 Testing

### Test Upload via cURL
```bash
curl -X POST http://localhost:8080/api/v1/documents/upload \
  -F "file=@sample.pdf"
```

### Test Chat via cURL
```bash
curl -X POST http://localhost:8080/api/v1/chat/message \
  -H "Content-Type: application/json" \
  -d '{
    "sessionId": "<session-id-from-upload>",
    "question": "What is this document about?"
  }'
```

### Use Browser UI
Simply open `http://localhost:8080` and use the web interface.

## 🐛 Troubleshooting

### 404 Error on http://localhost:8080
- Ensure Spring Boot is running: `docker-compose ps`
- Check logs: `docker-compose logs app`
- Wait 30 seconds for services to fully initialize

### "Timeout" Error During Chat
- Ollama is processing - first query takes longer (1-5 minutes)
- Check Ollama logs: `docker-compose logs ollama`
- Ensure your machine has sufficient CPU/RAM

### "multipart/form-data" Error
- Ensure you're uploading as multipart form data
- Content-Type should be handled automatically by the frontend
- For cURL: use `-F` flag (not `-d`)

### MongoDB Connection Refused
- Check MongoDB is running: `docker-compose ps mongodb`
- Verify credentials in application.yml
- Check logs: `docker-compose logs mongodb`

### Model Download Stuck
- Ollama is downloading llama3.1:8b on first run (4-5 GB)
- This is normal - takes 10-20 minutes
- Monitor: `docker-compose logs ollama`

## 📊 Response Timeout

The application is configured with **10-minute timeout** for chat responses to allow for:
- Complex document analysis
- Large PDF processing
- Slow LLM inference times

No timeout configuration is needed - responses may take minutes.

## 🛑 Stopping Services

```bash
# Stop all containers
docker-compose down

# Stop and remove data
docker-compose down -v

# View logs
docker-compose logs -f
```

## 📋 Build from Source

```bash
# Maven builds frontend + backend automatically
mvn clean package

# Generated JAR includes React frontend
# Output: target/PDFChatBot.jar
```

## 🔐 Security Notes

- File uploads limited to 1 MB (configurable)
- Only PDF files accepted
- CORS enabled for development
- Use HTTPS in production
- Secure MongoDB credentials in production

## 🌐 Supported LLM Models

Change in `application.yml`:
- `llama3.1:8b` (recommended, 8B parameters)
- `qwen2.5:7b` (faster, 7B parameters)
- `mistral:7b` (7B parameters)

## 📚 Dependencies

See `pom.xml` for complete list:
- spring-boot-starter-web
- spring-boot-starter-websocket
- spring-boot-starter-data-mongodb
- spring-ai-starter-model-ollama
- pdfbox
- springdoc-openapi (Swagger UI)
- sockjs-client (Frontend)
- @stomp/stompjs (Frontend)

## 📞 Support

For issues:
1. Check logs: `docker-compose logs`
2. Verify services: `docker-compose ps`
3. Test API: Use provided cURL examples
4. Check MongoDB: `docker exec document-qa-mongodb mongosh`

---

**Happy coding! 🚀**

