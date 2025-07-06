# Inspectra


## Overview

**Inspectra** is a lightweight monitoring tool, utilizing static analysis (using Taint Analysis) and Signature-based detection to detect and neutralize memory webshell (current development is to detect memshell techniques that focus on Tomcat web components and Spring framework components) on Apache Tomcat server.
<br>This repo includes source code and an executable JAR file of the tool.

## Features

- Detect memory webshell in Tomcat environment at real time.
- Automatically neutralize process creation ability of the memshell so that it cannot execute command on server.
- Raise alerts both on the console of the hosted JVM or write to a file in the form of simple JSON format which can be integrated with centralized security solution like SIEM for real-time monitoring.

## Technologies Used

- JDK 8 (can be used with JDK 11 but makes the detection slower because of Soot framework's set up).
- Java Agent implementation.
- Taint Analysis (Soot framework implementation).
- Signature-based Detection.


## Getting Started

### Prerequisites

Ensure you have the following installed on your system:

- JDK 8 (works best)
- Apache Tomcat 8/9, (compatible with JDK 8)

### How to run

1. Buid from source code

   - Clone the repository
   ```bash
   git clone https://github.com/M1nh-Duk/Inspectra.git
   cd Inspectra
   ```
   - Maven clean
   ```bash
     mvn clean install
   ```
   
   - Build project: Using your IDE or compile each file manually (^_^)
   - Package jar file
   
   ```bash
     mvn package
   ```
2. Or simply download the latest release
3. Config
   - Config folder path for upload folder of the web
   - Config whitelist classes that are either mistakenly detected classes or important classes of your web application that you don't want to be affect.
4. Usage
   ```bash
    Usage: java -jar Inspectra.jar [Options] [Flags]
    Options:
      1) attach [Java PID] - Attach to desired JVM process
      2) detach [Java PID] - Detach to desired JVM process
      3) list              - List all current JVM processes
      4) config            - Config server's upload folder and whitelist classes
                             (Note: For any config missing just press Enter).
    Flags:
      -auto: Automatically retransformed suspicious class and delete JSP file if found
      -silent: Do not print out to console
   ```
   
5. Example
   ``` bash
   EXAMPLES :
    java -jar Inspectra.jar attach 10001
    java -jar Inspectra.jar attach 10001 -auto
    java -jar Inspectra.jar attach 10001 -auto -silent
    java -jar Inspectra.jar attach 10001 -silent
    java -jar Inspectra.jar detach 10001
    java -jar Inspectra.jar list
    java -jar Inspectra.jar config

   ```

![Screenshot 2024-12-15 115428](https://github.com/user-attachments/assets/847bcc8c-db09-4979-9503-b54a009f7a76)

   

