@echo off
javac -cp "C:\Users\nalya\OneDrive\Bureau\IT\jar\servlet.jar" -d bin src\controller\*.java
jar cvf Framework.jar -C bin .
