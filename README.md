# O-MI-Security-Model
Complementary authorization and access control modules for O-MI

Dependencies
------------
- java-1.7
- apache-tomcat-8.0.28

Java libraries
--------------
- gson-2.4
- scribe-1.3.7
- sqlite-jdbc-3.8.11.2
- servlet-api-2.5.jar


Repository includes IntelliJ IDEA project

Usage
------------
0. Install Apache Tomcat [from here](https://tomcat.apache.org/download-80.cgi).
1. Download [latest zip](https://github.com/filiroman/O-MI-Security-Model/archive/master.zip) and extract it.
2. Run `install.sh` and specify your Apache Tomcat directory
<br/><b>!!! This will override your current Tomcat Settings !!!</b>
<br/>If you don't want them to be overwritten, manually edit `webapps/ROOT/WEB-INF/web.xml` and add the following <b>before</b> `</web-app>`:
```XML
<servlet>
   <servlet-name>TestAuth</servlet-name>
   <servlet-class>com.aaltoasia.TestAuth</servlet-class>
</servlet>
<servlet>
   <servlet-name>AccessControl</servlet-name>
   <servlet-class>com.aaltoasia.ACServlet</servlet-class>
</servlet>
<servlet>
   <servlet-name>PermissionService</servlet-name>
   <servlet-class>com.aaltoasia.PermissionService</servlet-class>
</servlet>

<servlet-mapping>
   <servlet-name>TestAuth</servlet-name>
   <url-pattern>/O-MI</url-pattern>
</servlet-mapping>
<servlet-mapping>
   <servlet-name>AccessControl</servlet-name>
   <url-pattern>/AC</url-pattern>
</servlet-mapping>
<servlet-mapping>
   <servlet-name>PermissionService</servlet-name>
   <url-pattern>/PermissionService</url-pattern>
</servlet-mapping>
```
3. Change default Tomcat port in `conf/server.xml` to 8088
4. Start Tomcat using `sh bin/startup.sh` command from Apache Tomcat installation directory

Now use http://localhost:8088/O-MI for Login and Register new users and http://localhost:8088/AC for Access Control.
