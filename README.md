
![Logo](https://github.com/Aviv13467/MyGarage/blob/main/src/resources/icons/logo_small.jpeg?raw=true)


# MyGarage

MyGarage is an intuitive, production-ready desktop application designed to streamline vehicle maintenance tracking, service histories, and garage analytics. Built with a modern, split-window user interface, the platform serves as a centralized hub for managing automotive repair workflows, tracking spare parts consumption, and generating real-time financial insights.


## Features

- Automated Service Records: Log detailed vehicle interventions, parts replaced, and exact labor costs in a searchable, lifelong service ledger.

- Smart Maintenance Alerts: An intelligent recommendation system that calculates exactly when a vehicle is due for its next oil change, brake service, coolant flush, or mechanical inspection 
based on mileage matrices and time elapsed.

- Live Financial Insights: Beautifully rendered expense charts that track operating costs over time, allowing users to analyze monthly spending or calculate total expenses within a custom date range.

- Lightning-Fast Lookups: A highly optimized search interface that handles extensive service histories smoothly, allowing instant vehicle, part, or invoice retrievals in real time.


## Target Audience (Who it's for)

- DIY Automotive Enthusiasts: Hands-on car owners who perform their own maintenance and want a professional tool to document precision service intervals, track specific part serial numbers, and monitor their long-term project budgets.

- Independent Auto Repair Shops: Small businesses and local mechanics looking to digitize their operations, manage active vehicle workflows, and maintain pristine, professional diagnostic histories for their clientele.
ֿ

## Installation

### Step 1: Clone the Project
Open your terminal, navigate to your working directory, and clone the repository:

```bash
  git clone https://github.com/Aviv13467/MyGarage.git
  cd MyGarage
```

Open the cloned folder as a project in IntelliJ IDEA.

### Step 2: Add the JavaFX Library to the Project

1. Download the JavaFX Cross-Platform SDK (matching your JDK version) from the official website and unzip it.

2. In IntelliJ, go to File ➔ Project Structure ➔ Libraries.

3. Click the '+' (New Project Library) ➔ Java.

4. Navigate to your unzipped JavaFX SDK folder, select the lib directory, and click OK.

### Step 3: Edit the Run Configurations (VM Options)

1. In the top toolbar next to the run button, click the application dropdown and select Edit Configurations...

2. Ensure MyGarage is selected on the left. Add it if it's not there already (Add New configuration ➔ Application ➔ Select MyGarage instead of Main Class).

3. Click Modify options and select 'Add VM options'.

4. In the new VM options input box, paste the following argument:
```
  --module-path /path/to/your/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml --enable-native-access=javafx.graphics
```
Note: Replace ```/path/to/your/javafx-sdk/lib``` with the actual, absolute file path to the JavaFX lib folder on your local machine.

### Step 4: Set Up the Database
The application relies on a local PostgreSQL instance to store vehicle and service history.

1. Open your PostgreSQL management tool (like pgAdmin or terminal).

2. Create a new database named mygarage.

3. Locate your database credentials (username and password) and port number (default is usually 5432 or 5433).

4. Execute your SQL schema script to generate the required tables (vehicles, parts, service_history, etc.).

### Step 5: Configure Database Credentials in Code
Before running the app, you need to point it to your local database instance.

1. In IntelliJ, open db.properties (located in the MyGarage/src/resources).

2. Update the connection strings with your local PostgreSQL credentials:
```
  private static final String DB_URL = "jdbc:postgresql://localhost:5433/mygarage"; 
  private static final String DB_USER = "your_postgres_username";
  private static final String DB_PASSWORD = "your_postgres_password";
```

### Step 6: Run the Application
Once the database configuration is updated, you can boot up the system directly from the source code:



Right-click the file and select Run 'MyGarage.main()'.

Note on Architecture: The application will automatically initialize the background Java RMI Server thread alongside the JavaFX User Interface.

The main dashboard will launch, and you can instantly start managing vehicles, tracking service histories, and view analytics!

## Screenshots

![App Screenshot](https://github.com/Aviv13467/MyGarage/blob/main/src/resources/screenshots/Screenshot%202026-05-30%20at%2018.57.39.png)


![App Screenshot](https://github.com/Aviv13467/MyGarage/blob/main/src/resources/screenshots/Screenshot%202026-05-30%20at%2018.57.43.png)


![App Screenshot](https://github.com/Aviv13467/MyGarage/blob/main/src/resources/screenshots/Screenshot%202026-05-30%20at%2018.57.47.png)


![App Screenshot](https://github.com/Aviv13467/MyGarage/blob/main/src/resources/screenshots/Screenshot%202026-05-30%20at%2018.57.54.png)


![App Screenshot](https://github.com/Aviv13467/MyGarage/blob/main/src/resources/screenshots/Screenshot%202026-05-30%20at%2018.57.57.png)


![App Screenshot](https://github.com/Aviv13467/MyGarage/blob/main/src/resources/screenshots/Screenshot%202026-05-30%20at%2018.58.01.png)

![App Screenshot](https://github.com/Aviv13467/MyGarage/blob/main/src/resources/screenshots/Screenshot%202026-05-30%20at%2018.58.10.png)

![App Screenshot](https://github.com/Aviv13467/MyGarage/blob/main/src/resources/screenshots/Screenshot%202026-05-30%20at%2018.58.16.png)



## License

<a href="https://www.flaticon.com/free-icons/fluid" title="fluid icons">Fluid icons created by Iconjam - Flaticon</a>
<a href="https://www.flaticon.com/free-icons/air-conditioner" title="air conditioner icons">Air conditioner icons created by Good Ware - Flaticon</a>
<a href="https://www.flaticon.com/free-icons/engine-oil" title="engine oil icons">Engine oil icons created by imaginationlol - Flaticon</a>
<a href="https://www.flaticon.com/free-icons/spark-plug" title="spark plug icons">Spark plug icons created by IYIKON - Flaticon</a>
<a href="https://www.flaticon.com/free-icons/automatic-transmission" title="automatic transmission icons">Automatic transmission icons created by Freepik - Flaticon</a>
<a href="https://www.flaticon.com/free-icons/motor" title="motor icons">Motor icons created by Eucalyp - Flaticon</a>
<a href="https://www.flaticon.com/free-icons/document" title="document icons">Document icons created by Freepik - Flaticon</a>
<a href="https://www.flaticon.com/free-icons/car" title="car icons">Car icons created by mynamepong - Flaticon</a>
<a href="https://www.flaticon.com/free-icons/home" title="home icons">Home icons created by Freepik - Flaticon</a>
<a href="https://www.flaticon.com/free-icons/box" title="box icons">Box icons created by Nhor Phai - Flaticon</a>
<a href="https://www.flaticon.com/free-icons/line-chart" title="line chart icons">Line chart icons created by Kirill Kazachek - Flaticon</a>
<a href="https://www.flaticon.com/free-icons/bell" title="bell icons">Bell icons created by Kiranshastry - Flaticon</a>

