# Leader Election with ZooKeeper

This project demonstrates leader election using Apache ZooKeeper and Spring Boot. It uses the Curator framework to interact with ZooKeeper for distributed coordination among multiple services.

## **Features**
- Elects one service as the leader.
- Other services act as workers.
- Monitors the leader node and re-elects a leader in case of failure.

---

## **Project Structure**
1. **`ZookeeperConfig`**  
   Configures the CuratorFramework to connect to ZooKeeper using the provided host and namespace.

2. **`LeaderElectionService`**  
   Handles leader election:
    - Creates an ephemeral `/leader` node with the `serviceId`.
    - Watches for node changes and re-elects a leader if needed.
    - Determines whether the current service is a leader or a worker.

3. **`ServiceController`**  
   Provides an endpoint `/status` to check whether the current service is a leader or worker.

4. **`application.yaml`**  
   Configures ZooKeeper connection details.

---

## **How It Works**
1. **Initialization**
    - Each service generates a unique `serviceId`.
    - Attempts to create the `/leader` node.

2. **Leader Election**
    - The first service to create `/leader` becomes the leader.
    - Other services read `/leader` to determine their role.

3. **Failure Handling**
    - If the leader fails, the `/leader` node is deleted.
    - Other services are notified, and a new leader is elected.

---

## **Endpoints**
- **`GET /status`**:  
  Returns `"This service is LEADER"` or `"This service is WORKER"`.

---

## **Example Workflow**
1. Start three services.
    - One becomes the leader, others are workers.
2. Stop the leader service.
    - A new leader is elected among the remaining services.

---

## **Dependencies**
- Java 11+
- Spring Boot
- Apache ZooKeeper
- Apache Curator

---
Here’s an updated **Testing** section for the README with commands included:

---

## **Testing**

1. **Setup**
    - Ensure ZooKeeper is running and accessible at the configured host (`192.168.184.129:2181`).
    - Start three instances of the service on different ports (e.g., `8080`, `8081`, `8082`).

2. **Role Verification**
    - Access the status endpoint of each service:
      ```
      http://localhost:8080/status
      http://localhost:8081/status
      http://localhost:8082/status
      ```
    - One instance should display:
      ```
      This service is LEADER
      ```
      While the others should display:
      ```
      This service is WORKER
      ```

3. **Simulate Leader Failure**
    - Stop the leader instance.
    - Refresh the `/status` endpoint of the remaining services to confirm a new leader has been elected.

4. **Service Rejoin**
    - Restart the stopped service.
    - Verify that it rejoins as a `WORKER` via its `/status` endpoint.

---