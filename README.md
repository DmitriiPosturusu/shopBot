# Telegram Bot E-commerce Application
This project implements a **Telegram Bot** for an e-commerce store, allowing users to browse and order food products using either Telegram buttons or an integrated virtual assistant powered by AWS Lex. The bot communicates with a Spring Boot backend, which processes user intents and actions, and interacts with a PostgreSQL database.

The bot can be deployed on-premises using Docker Compose or to the cloud using AWS Elastic Beanstalk for scalability and high availability. Additionally, the infrastructure is defined with Terraform to facilitate automated provisioning of resources like EC2, Elastic Beanstalk environments, S3, and more.

---

## Key Features
- **E-commerce platform**: Users can browse products, filter by categories, add items to their shopping bag, and place orders through Telegram.You can read more information about bot functionality here : [Bot Functionality Overview](https://github.com/DmitriiPosturusu/shopBot/blob/master/documentation/bot-functionality/ReadMeEn.md) .
- Here is demo video:
  [![Text](https://img.youtube.com/vi/7JNcmRekXM0/0.jpg)](https://www.youtube.com/watch?v=7JNcmRekXM0)

- **Multi-modal & Multi-language communication**: The system supports both manual interactions (via buttons) and NLP-based interactions (via text).User can interact in multiple languages. 
- Here is demo video:
  [![Text](https://img.youtube.com/vi/X97kkEO_X2U/0.jpg)](https://www.youtube.com/watch?v=X97kkEO_X2U)

- **AWS Comprehend**: (Planned for future) Enhancing bot’s NLP understanding by extracting entities and improving conversational flow.
- **AWS Lex voice recognition**: (Planned for future) Adding voice recognition for an even more engaging shopping experience.

---

## Technology Stack

### Backend and Application:
- **Java with Spring Boot**: Main framework for the backend application.
- **Spring Data JPA**: For managing database entities and interactions.
- **Spring Batch**: Used for processing large data uploads to the database using chunk processing.
- **PostgreSQL**: Relational database for persisting user data and order information.
- **Telegram API**: For communication between users and the bot.
- **Telegram Payments API**: For secure handling of payments.

### DevOps and CI/CD:
- **GitHub**: Code repository with version control.Environment variables and secret keys are managed through GitHub to securely handle credentials during the CI/CD pipeline.
- **GitHub Actions**: The CI/CD pipeline is streamlined into two GitHub Actions workflows:
   - **Build, Test, and Push**:
      - **Build**: Initiates by building the Maven project to compile the application code.
      - **Dockerize**: Constructs a Docker image of the application.
      - **Test in Docker Compose**: Launches a Docker Compose environment, deploying all necessary services (e.g., Spring Boot, PostgreSQL) to ensure compatibility and functionality.
      - **Push to Docker Hub**: Once testing is successful, the Docker image is pushed to Docker Hub for centralized storage and later use.
   - **Image Testing**:
      - **Pull**: Fetches the Docker image from Docker Hub.
      - **Test in Docker Compose**: Runs the image in a Docker Compose setup to validate its integrity and functionality.
- **Docker**: Containerization of services for easy deployment and scaling.
- **Docker Compose**: Local development with multiple containers (Spring Boot, PostgreSQL, Grafana, Prometheus, Alertmanager).
   - **Environment variables and health checks** for all services.
   - **Resource limits** for containers to optimize performance.

### Monitoring and Logging:
- **Prometheus**: Monitors system health, CPU, memory usage, and app/database status.
- **Grafana**: Visualizes metrics from Prometheus.
- **Alertmanager**: Sends real-time alerts via Telegram when something goes wrong (e.g., app down, resource spikes).
- **AWS CloudWatch**: Collects and monitors logs from the application.
- **New Relic**: Provides additional monitoring and performance insights.


### Storage:
- **AWS S3**: For storing files consumed by the Spring Boot app.
- **Terraform**: Used to automate the creation of AWS resources like EC2 instances, S3 buckets, security groups, and IAM roles and policies.

### Cost Optimization:
- **Docker Compose for Local Deployment**: For small-scale or testing purposes, Docker Compose can be used for efficient local deployment without incurring cloud costs.

- **AWS EC2 Scheduler**: Used to stop EC2 instances during non-business hours to save costs when the Spring Boot bot is not in use.
- **Auto-scaling**: Dynamically adjusts resources based on traffic and usage, optimizing costs.

---

## Deployment Options
1. **Local Deployment**:
   - **Docker Compose**: Clients can easily deploy the application locally using Docker Compose. This will spin up the Spring Boot app, PostgreSQL, Grafana, Prometheus, and Alert Manager as containers.
   - **Benefits**:
      - Easy setup with all services running locally.
      - Quick testing environment to monitor and evaluate different services.
        ![Text](https://github.com/DmitriiPosturusu/shopBot/blob/master/documentation/docker-compose.jpg?raw=true)

2. **Cloud Deployment**:
   - **Helm and Kubernetes**: Clients can leverage Helm charts and Kubernetes for a scalable cloud deployment.
      - **Benefits**:
         - Simplified management of deployments, updates, and rollbacks.
         - Scalability through Kubernetes pods and services for better load handling.
   - **AWS Elastic Beanstalk**: For automatic scaling, high availability, and managed infrastructure. Load balancers and auto-scaling help with seamless scaling and smooth deployments.
   - Alternatively, **AWS ECS** and **ECR** are available for container management, allowing easy scaling and management of Docker images in a production environment. All scaling is managed by AWS with security features, such as VPC, ensuring database isolation.
     ![Text](https://github.com/DmitriiPosturusu/shopBot/blob/master/documentation/cloud-beanstalk.jpg?raw=true)

---

## Best Practices:
- **CI/CD Pipeline**: Using GitHub Actions for an automated build, test, and deployment process.
- **Environment Variables**: Ensures sensitive data is protected and configuration is dynamic across environments.
- **IAM Roles and Policies**: Secure access control for AWS resources.
- **Encryption**: All communications between Spring Boot, AWS Lex, and PostgreSQL are encrypted for data protection.

---

## AWS Lex Integration
I created six intents with slot fields and added sample utterances. The slot fields are based on product categories. When a user sends a message to the bot, Spring Boot passes the message to AWS Lex, which determines the user's intent and responds accordingly. Based on the intent, Spring Boot queries the repository and displays the relevant products or order information to the user via the Telegram bot.

---

## Future Plans:
- **AWS Comprehend**: Integrating AWS Comprehend to improve the bot’s understanding of user messages and provide more personalized responses.
- **ELK Stack**: Adding the ELK stack (Elasticsearch, Logstash, Kibana) for improved logging, search, and troubleshooting capabilities.
- **Voice Recognition**: Utilizing AWS Lex’s voice recognition capabilities to make the shopping experience more interactive.

---

## Challenges Overcome:
- **Handling Large Data**: Using Spring Batch to upload large amounts of data efficiently with chunk-based processing.
- **Seamless Bot Interaction**: Integrated AWS Lex and Telegram APIs to handle complex user interactions both through buttons and text-based commands.
- **Scalable Cloud Infrastructure**: Deployed and managed infrastructure through AWS Elastic Beanstalk with Terraform automation, ensuring the system is ready for scaling.
- **Security and Monitoring**: Implemented a secure, monitored environment with real-time alerts to ensure system reliability and user trust.

---

**Thank you for taking the time to read this entire document!**

The biggest challenge for me wasn’t the code or deployment— it was writing this documentation. Now that it's done, I guess I’ve officially **documented** my success!