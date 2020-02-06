# Product Ingestion Library

## Overview
Code to parse and serialize grocery store products. Input file is fixed-width format, see ProductInformationIntegrationSpec.md for details. 

## How to Package
Prerequisite tools are:
- Maven, version 3.2.1 or higher
- Java version 7 or higher

The command 'mvn package' will compile, run tests, and package the code and libraries into an executable jar, found in the target/ directory
Other useful commands are:
- mvn compile
- mvn test (will also compile)

## How to Execute
java -jar ingestor-1.0-jar-with-dependencies.jar <path to input file>.
A test file is available under src/test/resources/input-sample.txt
The command above prints out the name of the file containing the output.

