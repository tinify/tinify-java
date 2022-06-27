[<img src="https://travis-ci.org/tinify/tinify-java.svg?branch=master" alt="Build Status">](https://travis-ci.org/tinify/tinify-java)

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
  <version>1.7.0</version>
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
