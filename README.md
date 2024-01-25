[![Maven Central](https://img.shields.io/maven-central/v/com.tinify/tinify.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.tinify%22%20AND%20a%3A%22tinify%22)
[![MIT License](http://img.shields.io/badge/license-MIT-green.svg) ](https://github.com/tinify/tinify-java/blob/main/LICENSE)
[![Java CI/CD](https://github.com/tinify/tinify-java/actions/workflows/ci-cd.yml/badge.svg)](https://github.com/tinify/tinify-java/actions/workflows/ci-cd.yml)

# Tinify API client for Java

Java client for the Tinify API, used for [TinyPNG](https://tinypng.com) and [TinyJPG](https://tinyjpg.com). Tinify compresses your images intelligently. Read more at [http://tinify.com](http://tinify.com).

## Documentation

[Go to the documentation for the Java client](https://tinypng.com/developers/reference/java).

## Installation

Install the API client via Maven:

```xml
<dependency>
  <groupId>com.tinify</groupId>
  <artifactId>tinify</artifactId>
  <version>1.8.8</version>
</dependency>
```

## Usage

```java
import com.tinify.*;
import java.io.IOException;

public class Compress {
  public static void main(String[] args) throws java.io.IOException {
    Tinify.setKey("YOUR_API_KEY");
    Tinify.fromFile("unoptimized.png").toFile("optimized.png");
  }
}
```

## Running tests

```
mvn test
```

### Integration tests

```
TINIFY_KEY=$YOUR_API_KEY mvn -Pintegration integration-test
```

## License

This software is licensed under the MIT License. [View the license](LICENSE).
