**ShopBot Docker Compose Setup** 

This project sets up a Docker Compose environment for a Spring Boot application, PostgreSQL, and monitoring tools like Prometheus and Grafana. It's suitable for small to medium deployments. For production, it's recommended to deploy services on separate servers (app, monitoring, and database).


**Services**

* **Postgres**: PostgreSQL database for the Spring Boot app.
* **PgAdmin**: GUI to manage PostgreSQL.
* **Postgres Exporter**: Exports PostgreSQL metrics for Prometheus.
* **Prometheus**: Monitoring and alerting tool.
* **Grafana**: Visualization of metrics from Prometheus.
* **Alertmanager**: Manages alerts from Prometheus.
* **Spring Boot App**: Main application.

**Setup Instructions**
1. Clone the repository:
`   git clone https://github.com/DmitriiPosturusu/shopBot.git
   cd shopBot/docker-compose`
2. Update a .env file in the docker-compose directory with your variables:
3. Run the Docker Compose setup:
   `docker-compose up -d`

_Prometheus Configuration_ (`prometheus.yml`):

Prometheus is set up with a 15-second scrape and evaluation interval. It scrapes metrics from two main targets:
1. Postgres Exporter on `postgres_exporter:9187`
2. Spring Boot App at `/actuator/prometheus on host.docker.internal:8080`

_Alertmanager Configuration_

The `alertmanager.yml` file is configured to send alerts via Telegram. Alerts are routed to the alert-manager receiver, with a repeat interval of 10 minutes.

**Accessing Services**
* PgAdmin: `http://localhost:5050`
* Prometheus: `http://localhost:9090`
* Grafana: `http://localhost:3000`
* Spring Boot App: `http://localhost:8080`
* Alertmanager: `http://localhost:9093`

**Notes for Production**

For production environments, it’s best to separate services across different servers:
* **Application**: Host the Spring Boot app separately.
* **Database**: Run PostgreSQL on a dedicated database server.
* **Monitoring**: Deploy Prometheus, Grafana, and Alertmanager on separate monitoring servers.