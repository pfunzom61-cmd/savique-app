# Savique — Personal Budget Tracker

Savique is a native Android budgeting application built to help users take control of their personal finances. The app enables seamless expense tracking, monthly budget target setting, and real-time financial data visualization through interactive dashboards and charts.

---

##  Video Demonstration & Screenshots

### [Watch the Full Walkthrough on YouTube](https://www.youtube.com/watch?v=a7sU380J10A)
*A comprehensive demonstration running on a physical Android device showcasing Firebase data synchronization, spending graphs, the progress dashboard, spending streaks, and CSV exporting.*


##  Tech Stack & Architecture

### Architectural Pattern: MVVM (Model-View-ViewModel)
The application strictly adheres to the **MVVM** pattern to decouple the presentation layer from business logic and data persistence, ensuring an extensible and testable codebase:
*   **Models:** Strong data definitions (`Expense`, `Category`, `Goal`, `User`) utilizing **Room** annotations for local persistence.
*   **DAOs (Data Access Objects):** Type-safe database queries strictly isolated and filtered by the active `userId` to ensure complete multi-tenant user data isolation.
*   **Repository Layer:** Acts as the single source of truth, managing and orchestrating data operations between local cache and cloud storage.
*   **ViewModels:** `BudgetViewModel` and `UserViewModel` handle state, exposing data streams via reactive **LiveData** structures to the UI.
*   **Views:** Lifecycle-aware Activities that observe data streams and update reactively with clean **ViewBinding**.

### Robust Data Strategy
Savique utilizes a robust **dual-database sync approach** to handle offline functionality effortlessly:
1.  **Local Cache (Room/SQLite):** All write operations hit the local SQLite cache first. This guarantees instant UI response times and complete offline capability.
2.  **Cloud Sync (Firebase Firestore):** A secondary, best-effort background worker syncs changes to Firestore, ensuring cross-device synchronization without blocking the main user interface or failing on unstable network connections.

---

##  Advanced & Custom Features

### 1. Gamified Spending Streak Engine
*   **What it does:** Encourages consistent financial logging habits by tracking consecutive operational days.
*   **How it works:** Upon logging an expense, the app evaluates the user's historical profile metrics (`lastLogDate` and `streak`) in Firestore. It programmatically increments the counter if the last entry was yesterday, resets it if a day was missed, and applies boundary checks to avoid double-counting multiple entries within the same calendar day.
*   **Location:** Real-time counter displayed prominently on the main Home Dashboard (`Streak: X days`).

### 2. CSV Financial Export Pipeline
*   **What it does:** Bridges mobile tracking with external desktop analysis suites like Microsoft Excel or Google Sheets.
*   **How it works:** Parses transactional database sets across any user-defined date period into structured comma-separated values (`Description`, `Amount`, `Date`, `Start Time`, `End Time`, `Category ID`). It handles the runtime generation of files directly to the native Android Downloads directory with unique timestamped file names (e.g., `savique_expenses_2026-05-31.csv`) or launches the system Native Share Sheet for external routing.
*   **Location:** Found directly inside the Expense History toolbar interface via dedicated **Save** and **Share** triggers.

### 3. Automated CI/CD (GitHub Actions)
The repository features an integrated automated pipeline (`.github/workflows/build.yml`) that triggers on every push or pull request to maintain build stability:
*   Deploys an automated Ubuntu workspace environment running **JDK 17 (Temurin)**.
*   Safely injects structural Firebase configurations (`google-services.json`) at runtime using encrypted **GitHub Secrets**.
*   Executes automated unit test coverage pipelines (`./gradlew test`) validation checks prior to generating executable debug builds.

---

##  Test Coverage
The project includes localized automated unit tests ensuring underlying financial calculation logic remains sound:
*   **`ExampleUnitTest`:** Structural baseline environment checking.
*   **`ExpenseCalculationTest`:** Exercises the mathematical arrays underlying dashboard totals and category breakdowns using localized mock data blocks to eliminate regression bugs in calculation logic.

---

##  Installation & Local Setup

1. Clone this repository onto your machine:
   ```bash
   git clone [https://github.com/YOUR_USERNAME/Savique-Budget-Tracker.git](https://github.com/YOUR_USERNAME/Savique-Budget-Tracker.git)
