# Pusula Service Tracker - Desktop Application

[![Java Version](https://img.shields.io/badge/Java-JDK%2024-blue.svg)](https://www.oracle.com/java/technologies/downloads/)
[![UI](https://img.shields.io/badge/UI-JavaFX-orange.svg)](https://openjfx.io/)
[![Database](https://img.shields.io/badge/Database-SQLite-lightgrey.svg)](https://www.sqlite.org/index.html)
[![Build](https://img.shields.io/badge/Build-Apache%20Maven-red.svg)](https://maven.apache.org/)

Pusula Service Tracker is a comprehensive desktop application developed in Java, designed to streamline and manage the daily operations of an Air Conditioning (AC) and home appliance technical service business. This project was initially conceived to support my brother's business, aiming to digitize and simplify processes such as preliminary accounting, service tracking, customer relationship management (CRM), parts tracking, and inventory management.

## Table of Contents

- [Project Overview](#project-overview)
- [Key Features](#key-features)
  - [Main Application Window](#main-application-window)
  - [Category Management (Brands, Models, Types)](#category-management-brands-models-types)
  - [Inventory Management](#inventory-management)
  - [Customer & Service Management](#customer--service-management)
- [Technologies Used](#technologies-used)
- [Architecture & Design Patterns](#architecture--design-patterns)
  - [MVC-like Structure](#mvc-like-structure)
  - [Data Access Objects (DAO)](#data-access-objects-dao)
  - [Database Management](#database-management)
  - [Transaction Management](#transaction-management)
- [Database Schema Highlights](#database-schema-highlights)
- [Screenshots](#screenshots)
- [Setup and Usage Notes](#setup-and-usage-notes)
- [Challenges & Learnings](#challenges--learnings)
- [Future Enhancements](#future-enhancements)
- [License](#license)
- [Contact](#contact)

## Project Overview

The primary goal of "Pusula Service Tracker" is to provide a user-friendly and efficient tool for small to medium-sized technical service businesses. It addresses the need for a centralized system to manage customer interactions, service job history, spare parts inventory, and essential financial records.

**Target Users:** Initially developed for my brother's technical service shop and its employees, with the potential to be adapted for similar small businesses.

**Scope:**
*   Customer record management (create, read, update, delete - CRUD).
*   Service record creation, listing, and tracking.
*   Spare part definition and management.
*   Inventory control (stock_in/stock_out operations).
*   Basic category management (e.g., appliance brands, models, types).

## Key Features

### Main Application Window
*   Intuitive tabbed interface (JavaFX `TabPane`) for easy navigation between modules.
*   Dynamic data refresh mechanism: Data in the active tab is updated automatically upon tab selection or after relevant operations.

### Category Management (Brands, Models, Types)
*   Separate `TableView`s for listing Brands, Models, and Types.
*   Filtering: Models can be filtered based on the selected Brand (via `ComboBox`).
*   CRUD operations for each category (Add New, Edit Selected, Delete Selected) through dialog-based forms.
*   Automatic list updates post-operation.

### Inventory Management
*   **New Part Entry:** Form for adding new spare parts, including selection of Brand, Model, and Type.
*   **Stock In/Out:** Dedicated forms for recording stock additions and consumptions, linked to specific parts.
*   **Stock Status View:** A `TableView` (utilizing `StokGorunum` DTO) displaying:
    *   Part Name, Brand, Model, Type
    *   Purchase Price, Sale Price
    *   Calculated current stock quantity.
*   Part Editing (Name, Prices via dialog/double-click) and Deletion (with confirmation).
*   Automatic updates to `ComboBox`es and the stock table after operations.
*   Input validation for numeric fields (price, quantity).

### Customer & Service Management
*   **New Customer Entry:** Form for adding new customer details.
*   **Customer Listing:** `TableView` for displaying registered customers.
*   Customer Editing (via dialog/double-click) and Deletion (with confirmation, warning about associated service records).
*   **New Service Record Form:**
    *   Customer selection via a searchable `TextField` and filtered `ComboBox`.
    *   Date selection using `DatePicker`.
    *   Description of the service performed.
    *   Optional: Used spare part selection (from `ComboBox`).
    *   Quantity input (mandatory if a part is selected).
    *   Labor cost and payment amount input.
    *   **Saving Service Record:** Invokes `ServisKaydiDAO` method which handles atomic stock deduction via transaction management.
*   **Past Service Records Listing:** `TableView` (utilizing `ServisGorunum` DTO) showing customer and part names for clarity.
*   Search functionality for the service records table (filter by Customer Name, Service Description, Part Name).
*   Automatic updates to relevant lists/tables post-operation.
*   Input validation for numeric fields.

## Technologies Used

*   **Programming Language:** Java (JDK 24)
*   **User Interface (UI):** JavaFX (FXML for layout, CSS for styling - *if applicable*)
*   **Database:** SQLite (single file: `servis.db`, managed locally)
*   **Build & Dependency Management:** Apache Maven (`pom.xml` for project structure, dependencies like `javafx-controls`, `javafx-fxml`, `sqlite-jdbc`)
*   **Packaging/Distribution:** `jpackage` (via `jpackage-maven-plugin`) to create platform-specific installers (e.g., .exe/.msi for Windows) bundling the JRE. (Requires WiX Toolset v3+ for Windows installers).
*   **Modularity:** Java Platform Module System (JPMS) with `module-info.java` for creating a custom runtime image using `jlink`.
*   **IDE:** [Apache NetBeans]

## Architecture & Design Patterns

The application loosely follows a Model-View-Controller (MVC) like architectural pattern:

### MVC-like Structure
*   **View:** FXML files (e.g., `MainView.fxml`, `KategoriView.fxml`) define the visual structure of the UI.
*   **Controller:** Java classes (e.g., `MainViewController.java`, `KategoriViewController.java`) linked to FXML files. They handle user interactions, perform input validation, implement business logic, and communicate with DAOs for database operations. They also update the View.
*   **Model:**
    *   **Entity Classes (POJOs):** Represent database tables (e.g., `Marka`, `Model`, `Musteri`, `Parca`, `ServisKaydi`).
    *   **Data Transfer Objects / ViewModels (DTOs):** Classes like `StokGorunum`, `ServisGorunum` used to hold complex or joined data for display in `TableView`s, often using JavaFX Properties for easy binding.

### Data Access Objects (DAO)
*   Dedicated DAO classes for each main entity (e.g., `MarkaDAO`, `MusteriDAO`, `ParcaDAO`).
*   Encapsulate all database-specific SQL queries and CRUD operations.
*   Manage database connections (using `DatabaseManager` and `try-with-resources` for statement and result set management).

### Database Management
*   A `DatabaseManager` class provides database connections (`getConnection`).
*   It also handles the initial creation of tables if they don't exist when the application starts (`createTablesIfNotExists`).
*   `PRAGMA foreign_keys = ON;` is executed to enable foreign key constraint enforcement in SQLite.

### Transaction Management
*   Manual transaction management (`setAutoCommit(false)`, `commit()`, `rollback()`) is implemented, particularly in `ServisKaydiDAO.addServisKaydi`, to ensure atomicity for operations like adding a service record and decrementing stock.

## Database Schema Highlights

The core entities and their relationships are managed through SQLite tables:
*   `markalar` (Brands: id, name)
*   `modeller` (Models: id, brand_id, name)
*   `tipler` (Types: id, name)
*   `musteriler` (Customers: id, name_surname, phone, address)
*   `parcalar` (Parts: id, name, brand_id, model_id, type_id, purchase_price, sale_price)
*   `stok_hareketleri` (Stock Movements: id, part_id, quantity, movement_type, date)
*   `servis_kayitlari` (Service Records: id, customer_id, date, description, part_id, quantity, labor_cost, payment_amount, profit)

Key constraints include Foreign Keys (with `ON DELETE CASCADE/RESTRICT/SET NULL` as appropriate) and `UNIQUE` constraints.

## Screenshots

*(Screenshots of the application's main interface, forms, and tables will be added here soon. This will help visualize the application's functionality.)*


## Setup and Usage Notes

*   **Prerequisites:** A Java Runtime Environment (JRE) compatible with JDK 24 (though `jpackage` aims to bundle this).
*   **Database:** The SQLite database file (`servis.db`) is automatically created in a hidden directory named `.PusulaServisTakip` within the user's home directory (`user.home`) on the first launch. This approach helps avoid write permission issues.
*   **Running the Application:**
    *   (Currently, the application can be launched via a `.bat` script.)
    *   The goal is to provide platform-specific installers (.exe for Windows) generated by `jpackage`. Fine-tuning of `jlink/jpackage` launcher arguments in `pom.xml` is in progress to perfect installer and shortcut creation.
*   **Icon:** An application icon (`.ico` file) is configured for the packaged application.

## Challenges & Learnings


Database Design & DAO:
"One of the initial challenges was designing a normalized relational database schema to accurately represent the complex relationships between services, parts, and inventory. This was addressed by carefully planning the data model and implementing the Data Access Object (DAO) pattern, which decoupled database logic from business rules and improved code modularity. Learning to effectively use SQLite's PRAGMA commands for enabling features like foreign key support was also a key takeaway."

JavaFX UI Complexity:
"Developing a responsive and intuitive user interface with JavaFX, especially for dynamic TableViews and inter-tab data synchronization, presented a learning curve. I overcame this by leveraging FXML for declarative UI design, mastering JavaFX Properties for data binding, and implementing a custom refreshData() mechanism with listeners to ensure UI consistency after operations. This significantly improved my understanding of event-driven programming in JavaFX."

Transaction Management:
"Ensuring data integrity during operations involving multiple database updates, such as recording a new service and simultaneously decrementing stock, was critical. I implemented manual transaction management (using setAutoCommit, commit, rollback) within the DAO layer. This was a valuable learning experience in managing atomic operations and handling potential exceptions to prevent data inconsistencies."

Packaging with jpackage:
"Configuring jpackage via the Maven plugin to create a bundled, platform-specific installer (e.g., .exe for Windows) with an embedded JRE and custom icon proved to be a detailed process. It required careful tuning of pom.xml configurations, understanding the Java Module System's interaction with jlink, and troubleshooting WiX Toolset integration. While still refining the launcher details, this significantly enhanced my knowledge of modern Java application deployment."

## Future Enhancements

The following features are planned for future development:
*   **PDF Report Generation:** Create printable service forms or inventory reports.
*   **Service Record Editing/Deletion:** Allow modification or removal of existing service records with appropriate checks.
*   **Advanced Stock Table Filtering:** Add more sophisticated filtering options to the inventory view.
*   **User Authentication:** Implement a login system for security and user roles.

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details.

## Contact

Developed by [Emircan KELEŞ] - [https://github.com/emirrkls]
*   Email: [emirrkeles@gmail.com]

---

This README aims to be a living document. Any contributions, suggestions, or feedback are welcome!
