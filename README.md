﻿# #taxi-service
#### `A simple web-app that represents the work of a taxi service`

**Description**

The application realized based on SOLID principles, performs CRUD, authentication and registration operations. The main purpose of this application is to keep records of taxi service drivers and cars.

It is written using N-tier architecture, used Custom Injector to achieve loose coupling.

**Project structure:**
1. DAO – Persistence Tier
1. Service - Business Logic Tier
1. Controller - Presentation Tier

**Features:**
-	registration a new driver, login (information on this operations will be logged in a local file)
- logout
- create, update and remove all models
- display list of all manufacturers, cars, current driver's cars, drivers

**Technologies:**
- MySQL 
- JDBC
- Java Servlet 
- Tomcat 9.0.50
- JSP
- JSTL
- Maven
- Log4j2
- JUNIT

**How to run application:**
1.	Clone the project to your IDE from GitHub.
2.	Create a DB, schemas and tables in your DBMS using data from file init_db.sql from package resources.
3.	Configure connection to DB in ConnectionUtil class with URL, username, password.
4.	Configure Tomcat (set "/" in deployment - taxi-service:war exploded).
5.	Search for logs/app.log in tomcat bin directory. 

**You can test the app by this** 
**[link](http://taxiservice-env-1.eba-5fm6pmcm.eu-west-3.elasticbeanstalk.com)**

(used [Amazon Elastic Beanstalk](https://aws.amazon.com/elasticbeanstalk/?nc1=h_ls) and [Amazon RDS](https://aws.amazon.com/rds/?p=ft&c=db&z=3))
