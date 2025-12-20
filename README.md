<div align="center">
  <img src="src/main/webapp/assets/images/logo.png" alt="logo_foundly" width="200" height="200" />
  <h1>Foundly</h1>
  <p>
    <b>The go-to platform for reporting, searching, and returning lost items.</b>
  </p>

  <p>
    <img src="https://img.shields.io/badge/Java-23-orange" alt="Java 23" />
    <img src="https://img.shields.io/badge/Jakarta%20EE-10-blue" alt="Jakarta EE 10" />
    <img src="https://img.shields.io/badge/Tomcat-11-yellow" alt="Apache Tomcat" />
    <img src="https://img.shields.io/badge/Database-MySQL-blue" alt="MySQL" />
    <img src="https://img.shields.io/badge/Build-Maven-C71A36" alt="Maven" />
  </p>
</div>

---

## 📖 About The Project

**Foundly** is a web application designed to digitize and simplify the process of returning lost items. 
We aim to build a network of honest citizens and local businesses (**Drop-Points**) collaborating to return what was lost in a **secure, traceable, and rewarding** way.

The system solves the issue of mistrust between strangers by introducing safe exchange points and a verification system (Secure Claim) based on specific questions.

---

## ✨ Key Features

### 👤 For Users (Finder & Owner)
- **Dual Role**: A single account to report found items or claim lost ones.
- **Secure Claim 🛡**: Anti-fraud system. The claimant must correctly answer verification questions set by the finder.
- **Lost Pets 🐾**: A dedicated section with specific fields for reporting lost pets.
- **Gamification 🏆**: Earn points and badges (e.g., *Sherlock Holmes*) for every successful return.

### 🏪 For Drop-Points (Businesses)
- **Business Registration**: Dedicated registration flow with business verification.
- **Custody Point**: Shops can offer themselves as safe locations for exchanges, increasing their local visibility.
- **Inventory Management**: Dashboard to track items in custody and manage check-in/check-out operations.

---

## 🏗 Architecture & Tech Stack

The project strictly follows the **MVC (Model-View-Controller)** architectural pattern to ensure maintainability and scalability.

### Backend
- **Language**: Java 23
- **Framework**: Jakarta EE 10 (Servlet 6.0)
- **Server**: Apache Tomcat 11.0.4
- **Security**: Password hashing via **BCrypt** (jBCrypt).
- **Data Access**: DAO (Data Access Object) pattern with custom Connection Pooling.
- **Service Layer**: Business logic completely separated from controllers.

### Database
- **DBMS**: MySQL (Hosted on Aiven Cloud)
- **Schema**: *Joined Inheritance* strategy for polymorphic report management (Objects vs. Animals).

### Frontend
- **Technology**: JSP (JavaServer Pages)
- **Styling**: Custom CSS3
- **Scripting**: JavaScript

---

## 🚀 Getting Started

To run Foundly locally in your development environment:

### Prerequisites
* JDK 23 or higher
* Apache Maven
* Apache Tomcat 11
* MySQL Server (or a connection to a Cloud DB)

### Installation

1.  **Clone the repository**
    ```bash
    git clone [https://github.com/YourUsername/Foundly.git](https://github.com/YourUsername/Foundly.git)
    cd Foundly
    ```

2.  **Database Setup**
    * Execute the `db/schema_v1.sql` script on your MySQL server to create the table structure.

3.  **Environment Configuration**
    * The project uses environment variables to secure credentials.
    * Configure the following in your IDE or OS:
        * `DB_PASSKEY`: Your MySQL user password.

4.  **Build & Run**
    ```bash
    mvn clean package
    ```
    * Deploy the generated `.war` file to your Tomcat server.
    * Access: `http://localhost:8080/Foundly`

## 👥 Authors

This project was developed by the **NC05 Team** for the Bachelor's Degree in Computer Science at the **University of Salerno**.

| Name | Role | Contact |
| :--- | :--- | :--- |
| **Salvador Davide Passarelli** | Backend & Architecture | [Email](mailto:s.passarelli2@studenti.unisa.it) |
| **Natale Nappi** | Frontend & UI/UX | [Email](mailto:n.nappi8@studenti.unisa.it) |
| **Salvatore Lepore** | DataBase & UI/UX| [Email](mailto:s.lepore11@studenti.unisa.it) |

<p align="right">
  <i>Supervisor: Prof. Carmine Gravino</i>
</p>

