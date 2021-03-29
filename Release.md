If something changes at `platform-share`, bump the version into its build.gradle.kts and execute
`./gradlew platform-shared:publish`.

Then release the plugins by executing:

```
./gradlew platform-test:publishPlugins
```

```
./gradlew kubernetes-test:publishPlugins
```
