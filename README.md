# B2B Shop API

## FE Link: https://github.com/esatakpunar/b2b-shop

## Tech Stack

**Framework**

* **Core**
    * Spring
        * Spring Boot 3
        * Spring Boot Test (JUnit)
        * Spring Boot Dev Tools
        * Spring Web
        * Spring Boot Actuator
        * Spring Doc OpenAPI
        * Spring Security 6
        * Spring OAuth2 Client
        * Spring OAuth2 Resource Server
    * Spring Data
        * Spring Data JPA

**3rd Party Dependencies**

* Lombok
* Test Containers
* MapStruct
* Easy Random
* JSON Web Token (JWT)
* JSON Web Signature (JWS)
* Bouncy Castle

**Database**

* MySQL

**Database Migration Tool**

* Liquibase

**Language**

* Java 17

**Build Tool**

* Maven

**Software Development Process**

* TDD
* Agile Kanban
* Jira

**Version Control**

* Git
* GitHub

**APIs Interaction Platform**

* Postman

**Application Pipeline**

* GitHub Actions

---

## Getting Started

The project is generated using Spring Initializer.

## Project Documents

### Postman

You can find the Postman collection in the main project file.

### Documentation

#### Overview

The B2B Shop API provides comprehensive backend services for B2B e-commerce platforms, including user management, product management, and order processing.

1. **Create System Owners and Login**
   - **Description:** Admin users are created with default credentials specified in the code when the project is initialized. Admins log in using only the username and password, while shop and customer users log in with `tenantId`, username, and password.
   
   ![Create System Owners and Login](/screenshots/1_Create_system_owners_and_login.png?raw=true)

2. **Create Shop**
   - **Description:** Admins enter necessary information to create a new shop. A default user is created with the shop's email address as the username and "password" as the password.
   
   ![Create Shop](/screenshots/2_Create_shop.png?raw=true)

3. **Create Category and Brand**
   - **Description:** Manage product categories and brands. Categories and brands are added to the system for organizing and classifying products.
   
   ![Create Category and Brand](/screenshots/3_Category_and_brand.png?raw=true)

4. **Create Product**
   - **Description:** Add new products to the system with details, pricing, and stock information.
   
   ![Create Product](/screenshots/4_Create_product.png?raw=true)

5. **Create Customer**
   - **Description:** Shop owners create new customer accounts. A default user is created with the customer's email address as the username and "password" as the password.
   
   ![Create Customer](/screenshots/5_Create_customer.png?raw=true)

6. **Create Address**
   - **Description:** Perform the address creation process.
   
   ![Create Address](/screenshots/6_Create_address.png?raw=true)

7. **Get Products**
   - **Description:** List existing products in the system and access their details.
   
   ![Get Products](/screenshots/7_Get_products.png?raw=true)

8. **Create Basket**
   - **Description:** Create a shopping basket where users can add and manage products.
   
   ![Create Basket](/screenshots/8_Create_basket.png?raw=true)

9. **Get Basket**
   - **Description:** Display the products in the user's shopping basket, including quantities and total price.
   
   ![Get Basket](/screenshots/9_Get_basket.png?raw=true)

10. **Create Order**
    - **Description:** Enter order information along with basket items to complete an order.
    
    ![Create Order](/screenshots/10_Create_order.png?raw=true)

11. **Get Orders**
    - **Description:** Display existing orders with status, dates, and details.
    
    ![Get Orders](/screenshots/11_Get_orders.png?raw=true)

12. **Get Dashboard**
    - **Description:** Show the overall system status, including order counts, revenue metrics, customer counts, and product counts. Data is retrieved through relevant queries and displayed on the dashboard.
    
    ![Get Dashboard](/screenshots/12_Get_dashboard.png?raw=true)

