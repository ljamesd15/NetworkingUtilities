## What is this repository?
This repo contains all the utility and tools that I have needed for local development. A source controlled location for scripts to increase local development velocity like DDNS or jobs that can be pulled locally and scheduled through crons/task schedulers.

### TODO:
1Set up JUnit tests and update workflow steps
2Migrate servers managed through ServerHealth check jobs to Kubernetes cluster for automatic restarts
3Move from AWS IAM User access Keys to Roles anywhere
4. Add helm charts for local servers to repository

### Workflow
Everything is run through Gradle. You can run test configuration through things like Intellij fairly simply however all builds and checks are integrated and run through Gradle.

1. Make your changes
2. Test your changes by directly compiling and running the classes or using Intellij test configurations
3. Validate the code passes the checkstyle and spotbugs requirements ```./gradlew check```
4. Compile the JAR ```$ mvn clean package```
5. Validate the jars run as expected using some of the test scripts found in <PACKAGE_ROOT>/scripts
6. Checkout changes to a feature branch and create a Pull Request
7. Once the PR has been approved merge it via Github
