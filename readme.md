# College Enrollment Management System Registrar Service
***
A REST web service API using Spring Boot Java for students to enroll into and drop courses, view class schedules, and view transcripts. This service is a component of a college enrollment management system.

### Co-repositories
- [Gradebook Service](https://github.com/cst438-team02-org/cst438-team02-gradebook)
- [Front-end](https://github.com/cst438-team02-org/cst438-team02-frontend)

## Features
***
- Student/admin login
- Student enrolls in courses
- Student drops enrolled courses
- Student views class schedule by year and semester to view course and grade information
- Student views college transcript to view history of courses taken and final grades
- Admin creates/updates/deletes users, sections, and courses

## Getting Started
***
This is one out of two services in the college enrollment management system. There also exists the [Gradebook Service](https://github.com/cst438-team02-org/cst438-team02-gradebook) and the [Front-end](https://github.com/cst438-team02-org/cst438-team02-frontend). To set up this system, start both Registar and Gradebook services, then start the Front-end following the instructions in the readme.md.
1. **Setup the Message Broker**

The Registrar and Gradebook services use RabbitMQ as the message broker to communicate with each other.
To setup and run RabbitMQ using a Docker container, perform the following steps.<br>
* **Pull the image**<br>
  ```docker pull rabbitmq:3-management```
* **Start the container on port 5672. Port 15672 is the management port**<br>
  ```docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management```

2. **Start the Registrar Service**<br>
The service will be available at
> http://localhost:8080