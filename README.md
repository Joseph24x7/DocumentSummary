# Document Q&A System - Spring Boot Backend

A modern Spring Boot 4.0.0 backend application for document Q&A powered by Ollama LLM. This application allows users to upload PDF documents, automatically generate summaries, and ask questions about the document content.

## 🎯 Features

- **📄 PDF Document Upload**: Upload PDF files (max 1 MB) with automatic text extraction
- **🤖 AI-Powered Summaries**: Generate document summaries automatically using local LLM
- **❓ Intelligent Q&A**: Ask natural language questions about uploaded documents
- **🔐 RAG Implementation**: Context-aware answers using Retrieval-Augmented Generation pattern
- **💾 MongoDB Storage**: Persistent document storage and retrieval
- **🐳 Docker Support**: Complete Docker Compose setup with Ollama and MongoDB
- **✅ Input Validation**: Comprehensive validation for all API inputs
- **🚨 Global Exception Handling**: Centralized error handling with detailed responses
- **📊 Logging**: Structured logging throughout the application

## 🏗️ Architecture

### Project Structure

```
document-summary/
├── src/
│   ├── main/
│   │   ├── java/com/docqa/
│   │   │   ├── controller/        # REST Controllers
│   │   │   ├── service/           # Business Logic
│   │   │   ├── repository/        # Data Access Layer
│   │   │   ├── model/             # JPA Entities
│   │   │   ├── dto/               # Data Transfer Objects
│   │   │   ├── util/              # Utility Classes
│   │   │   ├── exception/         # Exception Handling
│   │   │   ├── config/            # Spring Configuration
│   │   │   └── DocumentSummaryApplication.java
│   │   └── resources/
│   │       └── application.yml
│   └── test/
├── pom.xml
├── Dockerfile
├── docker-compose.yml
└── README.md
```

### Technology Stack

- **Java**: 21 (LTS)
- **Spring Boot**: 4.0.0
- **Spring Data MongoDB**: For persistent storage
- **Spring WebFlux**: WebClient for async HTTP
- **Apache PDFBox**: PDF text extraction
- **Ollama**: Local LLM integration
- **Docker**: Containerization

## 📋 API Endpoints

### 1. Upload Document & Generate Summary

```http
POST /api/v1/documents/upload
Content-Type: multipart/form-data

file: <PDF file>
```

**Response (201 Created)**:
```json
{
  "documentId": "507f1f77bcf86cd799439011",
  "summary": "This document discusses..."
}
```

**Status Codes**:
- `201 Created`: Document uploaded and processed successfully
- `400 Bad Request`: Invalid file type or size
- `500 Internal Server Error`: Processing error

### 2. Query Document

```http
POST /api/v1/documents/query
Content-Type: application/json

{
  "documentId": "507f1f77bcf86cd799439011",
  "question": "What is the main topic of this document?"
}
```

**Response (200 OK)**:
```json
{
  "answer": "The main topic of this document is..."
}
```

**Status Codes**:
- `200 OK`: Query processed successfully
- `400 Bad Request`: Missing or invalid documentId/question
- `404 Not Found`: Document not found
- `500 Internal Server Error`: Query processing error

## 🚀 Getting Started

### Prerequisites

- Docker & Docker Compose
- Java 21 (for local development)
- Maven 3.9+ (for building locally)

### Quick Start with Docker Compose

1. **Clone the repository**:
   ```bash
   cd DocumentSummary
   ```

2. **Build and start all services**:
   ```bash
   docker-compose up -d
   ```

   This will:
   - Start Ollama service (pulls llama3.1:8b model on first run)
   - Start MongoDB database
   - Build and start the Spring Boot application

3. **Verify services are running**:
   ```bash
   docker-compose ps
   ```

   Wait for all services to show "healthy" status (30-60 seconds).

4. **Test the application**:
   ```bash
   curl -X GET http://localhost:8080/api/v1/documents
   ```

### Manual Local Development Setup

#### 1. Start Ollama

```bash
# Using Docker
docker run -d -p 11434:11434 --name ollama ollama/ollama:latest
docker exec ollama ollama pull llama3.1:8b
```

Or install Ollama locally: https://ollama.ai

#### 2. Start MongoDB

```bash
docker run -d -p 27017:27017 \
  -e MONGO_INITDB_ROOT_USERNAME=root \
  -e MONGO_INITDB_ROOT_PASSWORD=example \
  --name mongodb mongo:7.0
```

#### 3. Build the Application

```bash
mvn clean package
```

#### 4. Run the Application

```bash
java -jar target/document-summary-1.0.0.jar
```

Or via Maven:
```bash
mvn spring-boot:run
```

## 📝 Configuration

### application.yml

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://mongodb:27017/document-summary

server:
  port: 8080
  servlet:
    context-path: /api/v1

ollama:
  base-url: http://ollama:11434
  model: llama3.1:8b

app:
  max-file-size: 1048576  # 1 MB
```

### Environment Variables

You can override configurations via environment variables:

```bash
export SPRING_DATA_MONGODB_URI=mongodb://user:pass@localhost:27017/db
export OLLAMA_BASE_URL=http://localhost:11434
export OLLAMA_MODEL=qwen2.5:7b
```

## 🔄 API Usage Examples

### Example 1: Upload a Document

```bash
curl -X POST http://localhost:8080/api/v1/documents/upload \
  -F "file=@sample.pdf" \
  -H "Accept: application/json"
```

**Response**:
```json
{
  "documentId": "507f1f77bcf86cd799439011",
  "summary": "This document provides a comprehensive overview of machine learning principles, covering supervised learning, unsupervised learning, and neural networks. Key algorithms and practical applications are discussed."
}
```

### Example 2: Query a Document

```bash
curl -X POST http://localhost:8080/api/v1/documents/query \
  -H "Content-Type: application/json" \
  -d '{
    "documentId": "507f1f77bcf86cd799439011",
    "question": "What are the main types of machine learning discussed?"
  }'
```

**Response**:
```json
{
  "answer": "The document discusses three main types of machine learning: supervised learning, unsupervised learning, and neural networks. Each type has different applications and approaches to training models."
}
```

## 🐛 Troubleshooting

### Ollama Service Won't Start

```bash
# Check Ollama logs
docker-compose logs ollama

# Ensure port 11434 is available
netstat -an | grep 11434  # On Windows
lsof -i :11434            # On macOS/Linux
```

### MongoDB Connection Issues

```bash
# Check MongoDB logs
docker-compose logs mongodb

# Verify MongoDB is running
docker exec document-qa-mongodb mongosh --eval "db.adminCommand('ping')"
```

### LLM Response is Slow

- Ensure Ollama has sufficient resources (CPU/RAM)
- The first query may take longer as the model loads
- Consider using a smaller model like `qwen2.5:7b`

### File Upload Fails

```bash
# Check file size
ls -lh sample.pdf

# Error: "File size exceeds maximum allowed size"
# Solution: Increase app.max-file-size in application.yml
```

## 🧪 Testing the APIs

### Using Postman

1. Import the following requests into Postman:

**Upload Document**:
- Method: `POST`
- URL: `http://localhost:8080/api/v1/documents/upload`
- Body: form-data with key "file"
- Select a PDF file

**Query Document**:
- Method: `POST`
- URL: `http://localhost:8080/api/v1/documents/query`
- Headers: `Content-Type: application/json`
- Body:
  ```json
  {
    "documentId": "<from upload response>",
    "question": "Your question here?"
  }
  ```

## 📊 Database Schema

### MongoDB Document Structure

```javascript
// documents collection
{
  "_id": ObjectId("507f1f77bcf86cd799439011"),
  "fileName": "sample.pdf",
  "mimeType": "application/pdf",
  "fileSize": 102400,
  "extractedText": "Document content here...",
  "summary": "Summary generated by LLM...",
  "uploadedAt": ISODate("2025-12-07T10:30:00Z"),
  "updatedAt": ISODate("2025-12-07T10:35:00Z")
}
```

## 🔒 Security Considerations

- File uploads are limited to 1 MB (configurable)
- Only PDF files are accepted
- Input validation on all API endpoints
- CORS is enabled for development (restrict in production)
- MongoDB credentials should be secured in production

## 🌐 Supported LLM Models

The application is tested with:
- `llama3.1:8b` (default, 8B parameters)
- `qwen2.5:7b` (7B parameters, lightweight)
- `mistral:7b` (7B parameters)

To change the model, update `application.yml` or set environment variable:
```bash
export OLLAMA_MODEL=qwen2.5:7b
```

## 📚 Dependencies

See `pom.xml` for complete dependency list. Key dependencies:

- `spring-boot-starter-web`: REST framework
- `spring-boot-starter-data-mongodb`: MongoDB integration
- `spring-boot-starter-webflux`: WebClient
- `pdfbox`: PDF extraction
- `lombok`: Code generation
- `jackson`: JSON processing

## 🔄 Development Workflow

### Building Locally

```bash
# Clean build
mvn clean package

# Skip tests for faster build
mvn clean package -DskipTests

# Run with Maven
mvn spring-boot:run

# Build Docker image
docker build -t document-qa:latest .
```

### Adding New Dependencies

```bash
# Add dependency
mvn dependency:tree
```

Edit `pom.xml` to add dependencies, then:
```bash
mvn clean install
```

## 🛑 Stopping Services

```bash
# Stop all services
docker-compose down

# Stop and remove volumes
docker-compose down -v

# View logs
docker-compose logs -f app
docker-compose logs -f ollama
docker-compose logs -f mongodb
```

## 📖 API Response Format

### Success Response (2xx)

```json
{
  "documentId": "...",
  "summary": "...",
  // or for query
  "answer": "..."
}
```

### Error Response (4xx, 5xx)

```json
{
  "status": 400,
  "message": "Invalid argument",
  "details": "File must be a PDF",
  "timestamp": "2025-12-07T10:30:00"
}
```

## 🚀 Performance Tips

1. **Model Selection**: Use smaller models (7B) for faster responses
2. **File Size**: Keep documents under 50 pages for better performance
3. **Batch Processing**: Process multiple files sequentially to avoid memory issues
4. **Hardware**: Allocate 8GB+ RAM to Ollama for optimal performance

## 📞 Support & Troubleshooting

For issues:
1. Check logs: `docker-compose logs app`
2. Verify all services are running: `docker-compose ps`
3. Check network connectivity between services
4. Ensure MongoDB and Ollama are healthy

## 📄 License

MIT License - See LICENSE file for details

## 🎓 Educational Use

This project is designed for learning Spring Boot, microservices, and LLM integration. Feel free to extend it with:
- Multiple file formats (DOCX, TXT, etc.)
- User authentication
- Vector embeddings for advanced RAG
- Streaming responses
- Document versioning

---

**Happy coding! 🚀**

