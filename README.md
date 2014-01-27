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
Build and install Middleware and Commons using one of the ff. methods:
  1.  Using the command line, go the IBPMiddleware/IBPCommons home directory, run the command: "mvn clean install -DskipTests=true".
  2.  From within Eclipse, right-click on the project, IBPCommons for instance, select Run As --> Maven build..., then input the target "clean install -DskipTests=true"

To Build
----------
To build the Fieldbook App using the command line, issue the following commands in the Fieldbook directory:
  1.  To create a clean build and run the test code: mvn clean package
  2.  To create a clean build without running the test code: Imvn -DskipTests=true clean package

To Run Tests
--------------
To run junit tests using the command line, issue the ff. commands in the Fieldbook directory:
  1.  To run all tests: mvn clean test
  2.  To run a specific test class: mvn clean test -Dtest=TestClassName
  3.  To run a specific function: mvn clean test -Dtest=TestClassName#functionName
 
To Deploy
-----------
  1.  Deploy the code after the build.
  2.  Or run via command line:
    a.  Configure and modify the settings.xml for the spring and set the profiles
    b.  In the pipeline/config, you should have a specific profile for the user, with the correct DB settings and properties.
    c.  Go to the Fieldbook folder
    d.  Make sure to edit the conf/settings.xml under the installation of maven (mvn -version to see the installation directory of maven)
    e.  mvn tomcat7:run

To Access Product
-------------------
Below are the urls to access the Fieldbook pages:
  1.  Home - http://&lt;tomcatHost&gt;:&lt;tomcatPort&gt;/Fieldbook/
  2.  Manage Trials - http://&lt;tomcatHost&gt;:&lt;tomcatPort&gt;/Fieldbook/TrialManager/
  3.  Manage Nurseries - http://&lt;tomcatHost&gt;:&lt;tomcatPort&gt;/Fieldbook/NurseryManager/
  4.  Ontology Browser - http://&lt;tomcatHost&gt;:&lt;tomcatPort&gt;/Fieldbook/OntologyBrowser/

Other Helpful Resources
-------------------------
To setup remote debugging:
  1.  In Windows: set MAVEN_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n
  2.  Go to IDE, setup remote debugging application, choose socket attach and input the correct port number.
  3.  Happy debugging

Thymeleaf Basic: http://www.thymeleaf.org/doc/html/Using-Thymeleaf.html
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