# SWEN 301 Assignment 3
## Java Servlet based network service for storing log information

### The following commands satisfy step 5 of the assignment handout:
1. ```mvn  clean install/package```
2. ```mvn jetty:run```
3. ```java -cp target/classes nz.ac.wgtn.swen301.a3.client.Client csv test.csv```

_Note by default the respective test.csv file will have no logger entries, but the functionality/doPost() is all working and have > 85% coverage from each test (used jacoco)_

### To stop the server, use this command in a new terminal tab/window:
1. ```mvn jetty:stop```

### URL's for each of the services
1. LogsServlet -> ```/logstore/logs```. _Ensure you have entered valid Limit and Level values e.g. ```logs?limit=10&level=info```_.
2. StatsCSVServlet -> ```/logstore/stats/csv```.
3. StatsExcelServlet -> ```/logstore/stats/excel```.
4. StatsHTMLServlet -> ```/logstore/stats/html```


*Final note: IGNORE the 'ERROR StatusLogger Log4j2 could not find a logging implementation. Please add log4j-core to the classpath.' We were not told to use Log4j and the functionality is still there*

