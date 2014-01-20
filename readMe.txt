BUILD PROCESS
1.  To build the the Fieldbook App, we can issue the following commands: 
  a.  mvn clean package - this would create a clean build and run the test code as well
  b.  mvn -DskipTests=true clean package - this would create a clean build without running test code

DEPLOY 
1.  We can deploy the code after the build.
2.  Or we can run it via the maven tomcat plugin: mvn tomcat7:run

CONFIGURE
1.  To configure, just modfy the settings.xml for the spring and set the profiles
2.  In the pipeline/config, you should have a specific profile for the user, with the correct DB settings and properties.

To setup remote debugging and save developer lots of time:

1.  In Windows: set MAVEN_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n
2.  Go to IDE, setup remote debugging application, choose socket attach and input the correct port number.
3.  Happy debugging

To run via command line:

1.  Go to the fieldbook folder
2.  Make sure to edit the conf/settings.xml under the installation of maven (mvn -version to see the installation directory of maven)
3.  mvn tomcat7:run

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