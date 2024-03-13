# Fearless
## Initial setup
Initialise custom resources:

```bash
$ mvn clean
$ mvn post-clean
```

## Running tests
Run `mvn compile` before running anything else, and re-run if any runtime resources are changed.

## Go Codegen
If you want to do Go Codegen you will need to uncomment the Go compiler fetching plugin in `pom.xml` and run `mvn post-clean`.
