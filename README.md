#### The application implementation has some assumptions:
1. All accounts have money in the same currency.
2. Transfer is allowed only between different accounts and with positive money value.
3. Account creation is allowed with none empty string identifier and none negative initial value (zero or more).

#### To build the application from source.
Maven and jdk 8 (at least) is necessary.	
- Build application (without test run):
	mvn clean package -DskipTests
- Run application:
	java -jar target/mt-1.0-SNAPSHOT.jar
- Run tests (It takes some time depending on current machine performance):
	mvn test
	
#### Test from browser:
All requests use HTTP GET method.
1. create account(s):
denis,nikolay - account identifiers
100 - initial balance
http://localhost:8080/accounts/create/denis/123.45
http://localhost:8080/accounts/create/nikolay/0
2. get account info:
denis - account identifier
http://localhost:8080/accounts/denis
3. transfer between accounts:
denis - source account,
nikolay - destination account,
100.45 - transfer amount
http://localhost:8080/accounts/denis/transfer/nikolay/100.45
		
#### Notes:
- Application use port 8080 by default,	to change port add input parameters "-port 9090", where 9090 is new port value:
	java -jar target/mt-1.0-SNAPSHOT.jar -port 9090
- In case log file is needed change mt\src\main\resources\log4j2.xml configuration and rebuild application.
