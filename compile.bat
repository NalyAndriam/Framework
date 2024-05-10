@echo off
javac -cp "C:\Users\nalya\OneDrive\Bureau\IT\jar\servlet.jar" -d bin src\controller\*.java
jar cvf "C:\Users\nalya\OneDrive\Bureau\IT\S3S4\S4\MrNaina\Framework.jar" -C bin .
