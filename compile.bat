@echo off
javac -cp "C:\Users\nalya\OneDrive\Bureau\IT\log\jar\servlet.jar" -d bin src\controller\*.java
jar cvf "C:\Users\nalya\OneDrive\Bureau\IT\S3S4\S4\MrNaina\testFramework\lib\Framework.jar" -C bin .
