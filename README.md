# Google Stax

[![GitHub Release](https://img.shields.io/github/v/release/google/stax?include_prereleases&style=flat-square)](https://github.com/google/stax/releases)
[![CI Build Status](https://img.shields.io/github/actions/workflow/status/google/stax/ci.yml?branch=main&style=flat-square)](https://github.com/google/stax/actions)
[![OpenSSF Best Practices](https://www.bestpractices.dev/projects/1234/badge)](https://www.bestpractices.dev/projects/1234)

**Google Stax** (formerly Evaluable AI) is a specialized platform for evaluating and scaling AI/ML applications. It provides developers with the tools to bootstrap infrastructure, manage evaluation workflows via Pub/Sub, and visualize performance metrics through a modern web interface.

---

## 🏗️ 1. Infrastructure Setup (Terraform)

Terraform manages the GCP resources including Cloud Run, Cloud SQL, and Pub/Sub.

### Prerequisites
* **GCP Project:** Create a project and enable billing. Note your `PROJECT_ID`.
* **Local Tools:** Install [Terraform](https://developer.hashicorp.com/terraform/install) and the [gcloud CLI](https://cloud.google.com/sdk/docs/install).
* **Docker Images:** Build and push your images to the Artifact Registry:
    * `us-central1-docker.pkg.dev/${PROJECT_ID}/cloud-run-source-deploy/stax-ui`
    * `us-central1-docker.pkg.dev/${PROJECT_ID}/cloud-run-source-deploy/stax-backend`

### Bootstrap Steps
1.  **Initialize GCloud:**
    ```bash
    gcloud init
    gcloud config set project $PROJECT_ID
    gcloud auth application-default login
    ```
2.  **Deploy Resources:**
    ```bash
    cd terraform/quickstart
    # Update project_id in main.tf
    terraform init -upgrade
    terraform plan
    terraform apply
    ```

---

## ⚙️ 2. Backend Server (Spring Boot)

The backend handles the core evaluation logic and integrates with the GCP Pub/Sub emulator for local development.

### Manual Prerequisites
* JDK 17+, Maven 3.9.6+, MySQL 8.x.

### Local Setup & Execution
1.  **Configure Environment:**
    ```bash
    cd server
    export AES_SECRET_KEY=defaultaessecret
    export TOKEN_SIGNING_KEY=default_token_signing_key
    export JDBC_DATABASE_URL="jdbc:mysql://localhost:3306/stax_db"
    export PROFILE_ACTIVE=local
    export GCP_PROJECT_ID=test
    ```
2.  **Run Pub/Sub Emulator:**
    ```bash
    gcloud components install beta pubsub-emulator
    gcloud beta emulators pubsub start --host-port=localhost:8085
    ```
3.  **Build & Run:**
    ```bash
    mvn clean install
    mvn spring-boot:run
    ```
    *App starts at `http://localhost:8080`.*

---

## 🖥️ 3. Frontend Application (Next.js)

A React-based UI built with Mantine and Tailwind CSS for interacting with the Stax platform.

### Setup & Execution
1.  **Install Dependencies:**
    ```bash
    cd frontend
    npm install
    ```
2.  **Environment Configuration:**
    * Copy `.env.template` to `.env.local` and set `NEXT_PUBLIC_API_BASE_URL` (e.g. `http://localhost:8080`) and `NEXT_PUBLIC_APP_BASE_URL` (e.g. `http://localhost:3000`).
    * Add `NEXT_PUBLIC_GOOGLE_CLIENT_ID` if using authentication.
3.  **Run Development Server:**
    ```bash
    npm run dev
    ```
    *Access the dashboard at `http://localhost:3000`.*

### Quality Control
* **Test:** `npm run test`
* **Lint:** `npm run lint:check`
* **Git Hooks:** `npm run githooks:init` (enforces quality on commit)

---

## 📂 Project Structure

```text
├── terraform/         # Infrastructure as Code (GCP)
│   ├── modules/       # Reusable GCP resource definitions
│   └── quickstart/    # Main deployment entry point
├── server/            # Spring Boot Java Server
│   └── src/           # Evaluation logic and API routes
└── frontend/          # Next.js & React Web App
    ├── app/           # App routes and page components
    └── components/    # Reusable UI components