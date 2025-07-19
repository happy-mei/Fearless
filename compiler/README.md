# Fearless
## Setup
1. Copy `src/utils/LocalResourcesTemplate.java` to `src/utils/LocalResources.java`,
   rename the class to `LocalResources`, and put in the relevant values. If you're using IntelliJ IDEA,
   you can leave the values as-is.

### Go Codegen
If you want to do Go Codegen you will need to run:
```sh
mvn generate-resources --file setup-go.xml
```
