cd "$(dirname "$0")"
javac -cp lib/sqlite-jdbc-3.51.3.0.jar src/Main.java src/DataLoader.java
java -cp lib/sqlite-jdbc-3.51.3.0.jar:src Main
read