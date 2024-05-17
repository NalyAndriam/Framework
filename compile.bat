@echo off
javac -cp "C:\Program Files\Apache Software Foundation\Tomcat 10.1\lib\servlet-api.jar" -d bin src\controller\*.java src\util\*.java
jar cvf "C:\Users\nalya\OneDrive\Bureau\IT\S3S4\S4\MrNaina\Framework.jar" -C bin .
