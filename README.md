# Personal Finance Manager

A desktop JavaFX application to help you track transactions, budgets, and reminders — complete with secure authentication, data visualizations, and export features.

---

## 📌 Features

- **User Authentication** – Login/Registration with hashed credentials (BCrypt).
- **Transactions** – Add, edit, delete, filter by month/year, and export to CSV.
- **Budgets** – Set category-wise limits and visualize usage with progress bars.
- **Dashboard** – Overview of balance, expenses, pie/line charts, and recent activity.
- **Reports** – Budget vs spent summary per category with bar chart.
- **Reminders** – One-time or recurring notifications.
- **SQLite Database** – Stores user data, test data, and more in `pfm.db`.

---

## 🛠️ Prerequisites

- Java 17 or later  
- Maven 3.8+  
- Git (optional, for cloning)

---

## 🚀 Getting Started

### Clone the repository
```bash
git clone https://github.com/Tj-Github30/personal-finance-manager.git
cd pfm
````

### Run the application

```bash
mvn clean javafx:run
```

---

## 🔑 Default Test Account

This project includes a default user to quickly try the application:

```
Username: testuser  
Password: password123
```

You can also register your own account after launching.

---

## 📁 Project Structure

```
/
├── pom.xml
├── pfm.db               ← SQLite database
├── src/
│   ├── main/
│   │   ├── java/        ← Java source code (controllers, services, DAO, etc.)
│   │   └── resources/   ← FXML, CSS, assets, database
│   └── test/            ← test files
└── README.md
```

---

## 🤝 Contributing

1. Fork this repository
2. Create a new branch: `git checkout -b feature/my-feature`
3. Make your changes and commit: `git commit -m "Add feature"`
4. Push to your fork: `git push origin feature/my-feature`
5. Submit a pull request

---
