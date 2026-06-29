#!/usr/bin/env bash
# Run Maven with Java 23 — required until Lombok supports Java 25
# Usage: ./mvn23.sh <mvn goals>   e.g.  ./mvn23.sh spring-boot:run
export JAVA_HOME=/Users/nomansbukhari/Library/Java/JavaVirtualMachines/openjdk-23.0.2/Contents/Home
mvn "$@"
