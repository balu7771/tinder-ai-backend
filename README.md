# Tinder AI Backend
Hi , This is Balaji Mudipalli,
I created this project so that I can learn how to integrate with AI. 
This will be deployed in AWS so that others can use as well. 

A Spring Boot application that simulates a Tinder-like backend with AI-powered features. This project was created based on the tutorial from [JavaBrains YouTube channel](https://youtu.be/k3fSQpz2Esg?si=eRGikIBwo1nLGGMS).

## Features

- Profile management with AI-generated profiles
- Conversation system with AI-powered responses
- Match creation and management
- OpenAPI/Swagger documentation

## Tech Stack

- Java 17
- Spring Boot 3.4.5
- Spring AI (OpenAI and Ollama integration)
- MongoDB for data storage
- SpringDoc OpenAPI for API documentation

## Prerequisites

- JDK 17+
- Maven
- MongoDB (via Docker Compose)
- OpenAI API key

## Getting Started

1. Clone the repository:
   ```
   git clone https://github.com/yourusername/tinder-ai-backend.git
   cd tinder-ai-backend
   ```

2. Set up environment variables:
   ```
   export SPRING_AI_OPENAI_API_KEY=your_openai_api_key
   ```

3. Run the application:
   ```
   ./mvnw spring-boot:run
   ```

4. Access the application:
   - API: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - OpenAPI JSON: http://localhost:8080/api-docs

## API Endpoints

### Profiles
- `GET /profiles/random` - Get a random profile

### Matches
- `GET /matches` - Get all matches
- `POST /matches` - Create a new match

### Conversations
- `GET /conversations/{conversationId}` - Get a conversation by ID
- `POST /conversations/{conversationId}` - Add a message to a conversation

## Deployment

The application can be deployed to AWS Elastic Beanstalk. See the deployment section for more details.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- [JavaBrains](https://www.youtube.com/c/JavaBrainsChannel) for the original tutorial
- Spring AI for the AI integration capabilities