# VaultFlow – Intelligent Cloud Storage & Collaboration Platform

> **A production-grade, full-stack Cloud Storage & Intelligence Platform** built with **Java 17, Spring Boot 3, React 18, TypeScript, Tailwind CSS, PostgreSQL, and Google Gemini AI**.

---

## 🌟 Key Architecture & Features

### 1. 🔐 Enterprise Authentication & Authorization
* **Stateless JWT Security**: Short-lived Access Tokens (15 min) + Refresh Token rotation stored in database.
* **BCrypt Password Hashing**: Adaptive cost factor password encoding.
* **Role-Based Access Control (RBAC)**: Fine-grained permissions (`OWNER`, `EDITOR`, `VIEWER`).

### 2. 📁 High-Performance Hierarchical File & Folder System
* **Materialized Path + Adjacency List Pattern**: Instant $O(1)$ ancestor breadcrumbs and subtree queries.
* **DFS Cycle Detection Algorithm**: Prevents circular reference parent assignment during folder moves.
* **Recursive Soft Deletes**: Moving a parent folder to trash automatically soft-deletes nested children.

### 3. 📄 Multi-Format File Management & In-Browser Preview
* **Multi-Format Processing**: Full lifecycle support for `PDF`, `DOCX`, `TXT`, `ZIP`, `PNG`, `JPEG`.
* **In-Browser Document & PDF Preview**: Interactive viewer modal for PDFs, images, code files, and text documents.

### 4. 🔍 High-Performance Search & AI Natural Language Intelligence
* **Multi-Criteria JPA Specifications**: Search by filename (`ILIKE`), file extension, and folder scope.
* **Gemini Flash NL Parser**: Translates natural language prompts (*"Find my PDF files larger than 2MB uploaded last week"*) into structured JPA query predicates (`minSizeBytes`, `maxSizeBytes`, `extension`, `daysAgo`).

### 5. 🔗 Granular Sharing & Public Access Portals
* **Tokenized Link Generation**: Secure 16-character cryptographically random link tokens.
* **Security Constraints**: Optional BCrypt password protection and configurable TTL expiration timestamps.

### 6. 📜 Immutable File Versioning Engine
* **Automated Snapshotting**: Automatic creation of version records ($v1$, $v2$, $v3$) on re-uploads.
* **Non-Destructive Restorations**: Revert to any historical version snapshot instantly.

### 7. ⚡ Asynchronous Audit Logging & Trash Lifecycle Engine
* **Spring `@EnableAsync` Event Bus**: Non-blocking audit logging for all user actions (`ActivityEvent` ➔ `ActivityEventListener`).
* **Automated Cron Purge**: `@Scheduled(cron = "0 0 3 * * ?")` 30-day soft delete cleanup scheduler.

---

## 🛠️ Tech Stack Overview

* **Backend**: Java 17, Spring Boot 3.3, Spring Data JPA, Spring Security 6, JJWT 0.12, Hibernate Criteria API.
* **Database**: PostgreSQL (Production) / Embedded H2 (Dev Zero-Config).
* **Frontend**: React 18, TypeScript, Tailwind CSS, Vite, Axios, Lucide Icons.

---

## 🚀 Quick Start Guide

### 1. Start the Backend Server

```bash
cd backend
mvn spring-boot:run
```

*Backend server runs at `http://localhost:8080`.*

### 2. Start the Frontend Application

```bash
cd frontend
npm install
npm run dev
```

*Frontend web app opens at `http://localhost:5173`.*

---

## 📄 License

Distributed under the MIT License. See `LICENSE` for more details.
