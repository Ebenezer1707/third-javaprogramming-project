# Java Currency Converter

A desktop application built with Java Swing that provides real-time currency conversion. It fetches live exchange rates from the [ExchangeRate-API](https://www.exchangerate-api.com) to perform accurate calculations.

This project demonstrates the use of Java for creating interactive GUI applications, handling asynchronous tasks to prevent UI freezing, and integrating with third-party web services.

---

## Screenshot

<img width="1918" height="1017" alt="Screenshot 2025-10-20 202851" src="https://github.com/user-attachments/assets/5ea1cdbf-558a-45ee-b1de-d14abc6b43d0" />



---

## Features

* **Real-Time Rates:** Fetches up-to-date exchange rates from a live API on application launch.
* **Full Conversion:** Converts any numeric amount from one currency to another.
* **Wide Currency Selection:** Supports all major global currencies provided by the API.
* **Swap Function:** Includes a "Swap" button (↑↓) to quickly reverse the 'From' and 'To' currencies.
* **Responsive UI:** Uses a `SwingWorker` background thread for all API calls, ensuring the user interface remains responsive and never freezes.
* **Input Validation:** Provides user-friendly error messages for invalid input (e.g., non-numeric text, negative numbers) and API failures.
* **Modern Look & Feel:** Features a custom, modern color palette and implements the "Nimbus" Look and Feel for a clean UI.

---

## How It Works (Technical Overview)

1.  **Initialization:** On startup, the application immediately disables the UI and initiates a `SwingWorker` background thread to fetch data.
2.  **Data Fetching:** The `SwingWorker` sends an HTTP GET request to the ExchangeRate-API using Java's modern `java.net.http.HttpClient`.
3.  **JSON Parsing:** The application receives a JSON response from the API. A custom parsing method (`parseRates`) manually parses this JSON string to extract all currency codes and their corresponding rates, storing them in a `HashMap`.
4.  **Populating UI:** Once the data is successfully fetched and parsed, the `SwingWorker` updates the UI on the main Event Dispatch Thread (EDT). It populates the "From" and "To" `JComboBox` (dropdown) menus and enables all UI components.
5.  **Conversion Logic:** When the user enters an amount and clicks "Convert":
    * The input is validated to ensure it's a positive number.
    * The application retrieves the rates for the selected 'From' and 'To' currencies from the `HashMap`.
    * It first converts the input amount to the base currency (USD) using the formula: `amountInBase = amount / fromRate`.
    * It then converts the base amount to the target currency: `convertedAmount = amountInBase * toRate`.
    * The final, formatted result is displayed in the large result panel at the bottom.

---

## Prerequisites

Before you run the application, ensure you have the following:

* **Java Development Kit (JDK):** JDK 11 or newer is required, as this project uses the `java.net.http.HttpClient` library.
* **Internet Connection:** An active internet connection is necessary to fetch the live exchange rates.

---

## How to Run

### 1. Configure the API Key

This project requires a free API key from [ExchangeRate-API](https://www.exchangerate-api.com).

**IMPORTANT:** The source code currently contains a hardcoded API key in the `API_KEY` constant. For security and to prevent quota-related issues, it is **highly recommended** that you get your own free API
key and replace the existing one:

**Dependencies**
This project is built using only standard Java libraries and requires no external .jar files or dependencies.

javax.swing

java.awt

java.net.http

java.util.concurrent


---

### 2. The Gitignore File (.gitignore)

This file is **essential** for a Java project. It tells Git to *ignore* files that shouldn't be uploaded, like compiled code (`.class` files) and IDE-specific settings.

Create a file named `.gitignore` (with the dot at the beginning) in the same folder and paste this in.

---

```git
# Compiled Java class files
*.class

# Log files
*.log
*.log.*

# Package files
*.jar
*.war
*.ear
*.zip
*.tar.gz
*.rar

# IDE-specific files
.idea/
.vscode/
*.iml
*.ipr
*.iws
.project
.classpath
.settings/
nbproject/
build/
dist/

# OS-generated files
.DS_Store
Thumbs.db

```java
// In CurrencyConverter.java
private static final String API_KEY = "YOUR_NEW_API_KEY_HERE";
