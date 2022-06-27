Compsci 677: Distributed and Operating Systems

Spring 2022

#**Lab 3**

## Requirements:
   Docker
   docker-compose

## Run Server in Docker:
   You can also use 'docker-compose down' and 'docker-compose up' to restart server while the data persists.

   - Run server
  
      ``cd src
      docker-compose up --build --detach
      ``
      
 - Check output

      ``cd src
      docker-compose logs --f
      ``
      
 - Stop Server

      ``cd src
      docker-compose down
      ``

## Run Tests:
   Change to Client folder
   ```
   cd src/client
   ```

   Find the properties file, set host name, port and number of requests for each propability run. We ran 100 requests by default for probabilities in range {0, 0.2, 0.4, 0.6, 0.8}

   From the command line, then run client.
   We ran 5 clients concurrently to test concurrency.

   ```
   .\mvnw spring-boot:run
   ```
   The client would request 100 request at every probability value, and wait for 10 seconds for server to restock, then continue. You can change these settings in
   ```
   src\client\src\main\java\edu\umass\client\ClientController.java
   ```

   To test server without cache, navigate to server frontend service
   ```
   src\frontend\src\main\java\edu\umass\toy\frontend\services\FrontEndService.java
   ```
   And comment out line 46-48 to avoid putting queries in cache
   


