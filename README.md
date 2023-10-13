![](https://img.shields.io/badge/java-21-darkgreen)
![](https://img.shields.io/badge/kotlin-1.9.10-blue)
![](https://img.shields.io/badge/archunit-1.1.0-pink)
![](https://img.shields.io/badge/junit-5.6.0-orangered)
[![Java 21 CI with Maven](https://github.com/allanneves/archunit-examples/actions/workflows/maven.yml/badge.svg)](https://github.com/allanneves/archunit-examples/actions/workflows/maven.yml)

# Examples of Code Design and Architectural Tests for Java Codebases using ArchUnit

## 🎯 Goal
This is a project for educational purposes to demonstrate how ArchUnit can be used for a variety of Architecture and Code Design tests in Java codebases.

## 🧭 How to use it
The project is a fictional software conference manager. The source code consists of six top-level packages:
- location -> different geographical places that can host a conference
- logging -> classic package for logging util found in many Java codebases
- presentations -> different types of presentations: local, international, virtual. The latter is not in this example.
- rooms -> small or large rooms available at the venue
- speakers -> speakers classified by expertise: Kotlin or Java
- tickets -> two-day, five-day or premium tickets for groups

I recommend that you clone the repo and open it in your IDE to run the tests as you will be constantly making changes to it. For simplicity, there is only one test suite to run: src/test/java/ArchUnitTests.kt

The expectation is that you will download the repo, run the tests, and remove the comments of the parts that violate test rules. By breaking the tests, you should have a good understanding of how ArchUnit works and the tests are written.

Alternatively, the code should be easy to read and you should be able to understand how ArchUnit does architecture tests without any issues.

## 🧪 What do the tests cover?
### 1) System.err.println() and System.out.println() should not be used.
- How do I break it? 
  - Uncomment the following lines: TwoDay.java:9 and FiveDay.kt:9
- Why should I test it?
  - Standard streams should only be used for debugging. Logging libraries are more performant and specialized.

### 2) Generic exceptions should not be thrown.
- How do I break it?
  - Uncomment the line 7 in InternationalPresentation.java
- Why should I test it?
  - Generic Exceptions are rarely useful and can very often be dangerous to an application at Runtime.

### 3) Interfaces should not have the word interface in their name
- How do I break it? 
  - Rename the interface Presentation.java to PresentationInterface.java
- Why should I test it?
  - It is usually a bad practice to name interfaces like "ISomething" or "SomethingInterface"

### 4) Speaker class should always be abstract
- How do I break it?
  - Remove the abstract keyword from the class Speaker.java
- Why should I test it?
  - This will depend on your use case, but comes in handy when you are refactoring a large codebase and want to lock a type of hierarchy.

### 5) Presentation interface should only have two implementations
- How do I break it?
  - Add a new implementation to the Presentation interface
- Why should I test it?
  - Similar to the above, one of the goals of refactoring a large codebase might be to lock the amount of implementations to remove duplication and unnecessary code abstractions created in the past. Java 17 already offers sealed classes that might be an alternative way of achieving the same, but if you are locked into an older version of Java this might be a ideal solution for closing the interface to new implementations.

### 6) LocalPresentation should not depend on InternationalPresentation
- How do I break it?
  - Uncomment line 6 in LocalPresentation.java
- Why should I test it?
  - These tests are highly effective in preventing cyclic dependencies and codebase entanglements. They enable you to imbue your code with semantic understanding, ensuring that the business logic preventing components from communicating with each other is upheld. This is a valuable mechanism for initiating the separation of components, breaking them down into microservices, and ensuring they adhere to the design.

### 7) Classes in location package should not depend on classes in speakers package
- How do I break it?
  - Uncomment line 9 in Belgium.java
- Wy should I test it?
  - Similar to the above, this test is a valuable assurance that classes from different packages will not depend on each other, avoiding tangles and cyclic dependencies. The goals may vary but are usually centered around microservices and code quality. 

### 8) Room implementation should have a serialVersionUID field
- How do I break it?
  - Delete the line 7 in SmallRoom.java
- Why should I test it?
  - Serialization in Java is usually undesired and a legacy side effect. If you are dealing with a codebase that uses Java Serialization, you want to guarantee consistency across the classes using it. This test is especially helpful to detect changes to the serialVersionUID field before they are in Production.

### 9) All package names should be in lower case
- How do I break it?
  - Rename any package within src/main/java to contain one or many uppercase characters
- Why should I test it?
  - This is a basic Java naming convention and should naturally be caught during code review. However, changes might slip through code review and having the tests re-enforcing the rule is always reassuring.

### 10) Methods annotated with LocationInfoStreamer should have between 1 and 3 arguments
- How do I break it?
  - Uncomment lines 26-34 in Canada.java
- Why should I test it?
  - This is a useful test to check if methods comply with certain rules when an annotation is applied. My example contains a custom annotation, which could be related to Logging, Kafka Streams, Spring, Guice, and so on. These checks are not possible during compile time and ArchUnit assures that your validation will be respected before your code is in Production.

## 📚 Test Blueprint and Design
- Single test suite: as this repo serves educational purposes, having a single test helps us with simplicity and effective communication 
- Uses JUnit 5 and ArchUnit 1.1.0
- ArchUnit tests might be expensive to run, so we create only a single instance per class with @TestInstance:
```kotlin
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
```
- Uses a default ArchUnit import option to ignore test classes, so they are not included in the analysis and picked up by ArchUnit. I also added a custom option to ignore classes in our logging folder, just as an example of a package that we might not be interested in testing with ArchUnit:
```kotlin
@AnalyzeClasses(
    packages = ["com.conference"],
    importOptions = [DoNotIncludeLogging::class, DoNotIncludeTests::class]
)

private class DoNotIncludeLogging : ImportOption {
    override fun includes(location: Location?): Boolean {
        return when (location) {
            null -> false
            else -> !location.contains("logging")
        }
    }
}
```
- Contains an example of a custom ArchUnit Class Condition to check for a valid serialVersionUID field:
```kotlin
private class HaveAValidSerialVersionUidField() : ArchCondition<JavaClass>("have a valid serialVersionUID field") {
    override fun check(item: JavaClass?, events: ConditionEvents?) {
        val serialVersionField = item?.getField("serialVersionUID")

        val isSerialVersionValid = (HasModifiers.Predicates.modifier(JavaModifier.STATIC).test(serialVersionField)
                && HasModifiers.Predicates.modifier(JavaModifier.FINAL).test(serialVersionField)
                && HasType.Predicates.rawType(UUID::class.java).test(serialVersionField))

        events?.add(SimpleConditionEvent(item, isSerialVersionValid, "serialVersionUID is not valid"))
    }
}
```
- Contains an example of a custom ArchUnit Method Condition to check for method arguments:
```kotlin
private class HaveValidArguments() :
    ArchCondition<JavaMethod>("methods using LocationInfoStreamer annotation should have valid parameters") {

    override fun check(method: JavaMethod, events: ConditionEvents) {
        val numberOfArgumentsRule = object {
            val message = "method ${method.name} should specify between 1 and 3 arguments when annotation is used"
            val isValid = method.parameters.size in 1..3
        }

        if (!numberOfArgumentsRule.isValid) {
            events.add(SimpleConditionEvent.violated(method, numberOfArgumentsRule.message))
        }
    }
}
```
