Fieldbook
============

Overview
----------
The Fieldbook tools provide a user-friendly interface for creating, editing and deleting studies and experiments. 
Such studies may be nurseries, trials, hybridization experiments and other types. These tools make use of our middleware and 
database to enable integration and comparative evaluation between and among other studies that were entered or available in 
our system.

Prerequisites
---------------
Build and install Middleware and Commons 
* On the command line, go the IBPMiddleware/IBPCommons home directory, run the command: "mvn clean install -DskipTests=true".
* From within Eclipse. To do this, right-click on the project, IBPCommons for instance, select Run As --> Maven build..., then input the target "clean install -DskipTests=true"

To Build
----------
* To build the Fieldbook App, issue the following commands in the Fieldbook directory:
  a.  mvn clean package - this would create a clean build and run the test code as well
  b.  mvn -DskipTests=true clean package - this would create a clean build without running test code

To Run Tests
--------------
* To run all tests, go to the Fieldbook directory and run the ff. command: mvn clean test 
* To run a specific test class: mvn clean test -Dtest=TestClassName
 
To Deploy
-----------
* Deploy the code after the build.
* Or run via command line:
  1. Configuration
    a.  To configure, just modify the settings.xml for the spring and set the profiles
    b.  In the pipeline/config, you should have a specific profile for the user, with the correct DB settings and properties.
  
  2. Running
    a.  Go to the Fieldbook folder
    b.  Make sure to edit the conf/settings.xml under the installation of maven (mvn -version to see the installation directory of maven)
    c.  mvn tomcat7:run

To Access Product
-------------------
* Below are the urls to access the Fieldbook pages: 
1. Home - localhost:8080/Fieldbook
2. Manage Trials - localhost:8080/Fieldbook/TrialManager
3. Manage Nurseries - localhost:8080/Fieldbook/NurseryManager
4. Ontology Browser - localhost:8080/Fieldbook/OntologyBrowser/

Other Helpful Resources
-------------------------
* To setup remote debugging:
1.  In Windows: set MAVEN_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n
2.  Go to IDE, setup remote debugging application, choose socket attach and input the correct port number.
3.  Happy debugging

*Thymeleaf Basic: http://www.thymeleaf.org/doc/html/Using-Thymeleaf.html
1.  th:text -> escaped text, it will be use to substitute the body
2.  th:utext -> unescaped text, it will be use to substitute the body
3.  #{message.home}" -> would be use for internationalization
4.  ${today} -> similar to spring expression language

    Text literals: '...'
    Number literals: 0, 34, 12, 3.0, 12.3, etc.
    Simple expressions:
        Variable Expressions: ${...}
        Selection Variable Expressions: *{...}
        Message Expressions: #{...}
        Link URL Expressions: @{...}
    Binary operations:
        String concatenation: +
        Arithmetic operators: +, -, *, /, %
        Comparators: >, <, >=, <=
        Boolean operators: and, or
        Equality operators: ==, !=
    Unary operations:
        Minus sign (numeric): -
        Boolean negation: !, not
    Conditional operators:
        If-then: (if) ? (then)
        If-then-else: (if) ? (then) : (else)
        Default: (value) ?: (defaultvalue)

		mvn clean install -DskipTests -DenvConfig=ci