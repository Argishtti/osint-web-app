# OSINT Scanning Service – Full Stack

This repository contains a full-stack OSINT scanning service with:
- **Backend**: Kotlin-based Spring Boot app
- **Frontend**: React app for interacting with the scan service

Each project lives in its own directory:
```
my-fullstack-task/
├── backend/    # Kotlin + Spring Boot
└── frontend/   # React
```

---

## 📁 Project Structure

| Folder     | Description                   |
|------------|-------------------------------|
| `backend/` | Kotlin Spring Boot app        |
| `frontend/`| React frontend app            |

---

## 🔧 Backend: Kotlin Spring Boot

This project is a Kotlin-based Spring Boot application designed to manage and run OSINT (Open-Source Intelligence) scans, using a PostgreSQL database and Amass for subdomain enumeration. The project is containerized using Docker and uses Gradle for build management.

### 🚀 Tech Stack

- **Kotlin (JDK 21)**
- **Spring Boot 3.4.4**
- **PostgreSQL 15 (Docker)**
- **Amass (Docker)**
- **Gradle**
- **Docker Compose**

### 🐳 Getting Started with Docker

#### Prerequisites

- [Docker](https://www.docker.com/get-started)
- [Docker Compose](https://docs.docker.com/compose/)

#### Start Services

```bash
cd osint
docker-compose up
```

After that run application from:

osint\src\main\kotlin\com\ptbox\osint\OsintApplication.kt

This will spin up:
- **PostgreSQL** on port `5433` (DB: `osintdb`, User: `osintuser`, Password: `osintpass`)
- **Amass** container for domain enumeration (starts idle by default)

> To interact with Amass, you can `docker exec -it amass /bin/sh`.

#### Stop Services

```bash
docker-compose down
```

### ⚙️ Build and Run

#### Prerequisites

- [JDK 21+](https://adoptium.net/)
- [Gradle 8+](https://gradle.org/) (or use the wrapper)

#### Run the Application

```bash
./gradlew bootRun
```

#### Build the Application

```bash
./gradlew build
```

### 🛠️ Custom Notes

- The Amass container is set up with a persistent `/config/amass` volume and runs indefinitely with `tail -f /dev/null`.
- PostgreSQL uses a persistent Docker volume: `postgres-osint-data`.

---

## 🌐 Frontend: React

This is a React-based UI for managing scans and displaying results.

### 🛠️ Tech Stack

- **React 18**
- **Vite** (for fast dev builds)
- **Tailwind CSS**
- **Axios** (for API calls)
- **React Router** (for navigation)

### 🚀 Getting Started

#### Prerequisites

- [Node.js](https://nodejs.org/) (v18+ recommended)
- [Yarn](https://yarnpkg.com/) or npm

#### Run the App

```bash
cd frontend
yarn install     # or npm install
yarn dev         # or npm run dev
```

The app will start on `http://localhost:5173`.

> ⚠️ You may need to update `vite.config.js` or `.env` to point to the correct backend URL (e.g., `http://localhost:8081`).

#### Build for Production

```bash
yarn build       # or npm run build
```

---

## 🧾 License

This project is open-source and licensed under the MIT License.
