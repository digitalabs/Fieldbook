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
Build and install Middleware and Commons using one of the following methods:
  1.  Using the command line, go the IBPMiddleware/IBPCommons home directory, run the command: "mvn clean install".
  2.  From within Eclipse, right-click on the project, IBPCommons for instance, select Run As --> Maven build..., then input the target "clean install"

To Build
----------
To build the Fieldbook App using the command line, issue the following commands in the Fieldbook directory:
  1.  To create a clean build and run the test code: mvn clean package

To Run Tests
--------------
To run junit tests using the command line, issue the following commands in the Fieldbook directory:
  1.  To run all tests: mvn clean test
  2.  To run a specific test class: mvn clean test -Dtest=TestClassName
  3.  To run a specific test function: mvn clean test -Dtest=TestClassName#testFunctionName
 
To Deploy
-----------
  1.  Deploy the code after the build.
  2.  Or run via command line:
  
    Configuration
  	* Go to your maven installation, you can find your installation directory by typing in DOS "mvn -version", this should show you the MVN installation information
  	* Go to the Maven installation directory conf/settings.xml, modify as necessary and set the profiles.
  	* In the Fieldbook directory, go to pipeline/config, you should have a specific profile for the user, with the correct DB settings and properties.
  	
  	Running via command line
  	* From the command line, go to the Fieldbook folder
  	* Execute the ff: mvn tomcat7:run

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