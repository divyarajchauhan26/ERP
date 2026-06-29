# University ERP System

A comprehensive Enterprise Resource Planning (ERP) system designed for university management. Built with Java, Swing (FlatLaf), and MySQL.

---

## 🚀 Features
- **Role-Based Access Control:** Distinct dashboards and permissions for Admins, Instructors, and Students.
- **Modern UI:** Sleek dark mode interface powered by FlatLaf.
- **Robust Database Architecture:** Dual-database design separating authentication (`university_auth_db`) from core ERP operations (`university_erp_db`).
- **Data Visualizations:** Built-in charting using JFreeChart.

---

## 🛠️ Prerequisites
- **Java Development Kit (JDK):** Version 17 or higher.
- **MySQL Server:** Version 8.0 or higher.
*(Note: You do not need Maven pre-installed, as this project includes the Maven Wrapper `mvnw`!)*

---

## 📦 Installation & Setup

### 1. Database Setup
The system relies on two MySQL databases. Two SQL dump files are included in the root directory to set these up instantly:
1. Open your MySQL client (e.g., MySQL Workbench or CLI).
2. Run `auth_dump.sql` to create the `university_auth_db` schema and default users.
3. Run `erp_dump.sql` to create the `university_erp_db` schema (courses, students, system settings).

### 2. Configure Database Credentials
Open the database configuration file located at:
`src/edu/univ/erp/data/DatabaseConnector.java`

Update the password constants to match your local MySQL server's root password:
```java
// --- Auth DB Connection ---
private static final String AUTH_DB_PASSWORD = "YOUR_MYSQL_PASSWORD";

// --- ERP DB Connection ---
private static final String ERP_DB_PASSWORD = "YOUR_MYSQL_PASSWORD";
```

---

## ⚡ How to Run

### Windows (Quickest Way)
Simply double-click the `run.bat` file located in the root folder. It will automatically download Maven dependencies (if running for the first time), compile the project, and launch the GUI!

### Terminal / Command Line (Any OS)
You can use the bundled Maven Wrapper to launch the application directly from the terminal:
```bash
# On Windows:
.\mvnw.cmd exec:java -Dexec.mainClass="edu.univ.erp.Main"

# On Linux / macOS:
./mvnw exec:java -Dexec.mainClass="edu.univ.erp.Main"
```

---

## 🔑 Default Login Credentials

Use the following test accounts to explore the different role-based views. (All passwords are securely hashed using BCrypt in the database).

| Role | Username | Password |
| :--- | :--- | :--- |
| **Admin** | `admin1` | `adminpass` |
| **Instructor** | `inst1` | `instpass` |
| **Student** | `stu1` | `stupass1` |
| **Student** | `stu2` | `stupass2` |

---

## 💡 Troubleshooting
- **`Connection Refused` / `Access Denied`**: Ensure MySQL is actively running and the passwords in `DatabaseConnector.java` match your MySQL root password.
- **`Table 'users_auth' doesn't exist`**: Ensure you imported `auth_dump.sql` before launching the app.
- **Maintenance Mode Banner**: If you see an orange/red maintenance banner on login, log in as `admin1` and toggle Maintenance Mode OFF in the System Settings tab.
