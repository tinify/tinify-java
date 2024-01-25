## 1.8.8
* Fixed transient dependencies for okhttp

## 1.8.7
* Fixed Import-Package manifest for OSGI deployments

## 1.8.6
* Fixed Import-Package manifest for OSGI deployments

## 1.8.5
* Upgrade Okhttp to version 4.12.0

## 1.8.4
* Upgrade Okio to version 3.7.0

## 1.8.3
* Fix for requests with empty body

## 1.8.2
* Fixed Import-Package manifest for OSGI deployments

## 1.8.0
* Added new property extension().
* Added new methods convert(new Options().with("type", "image/webp")) and
  transform(new Options().with("background", "black")).

## 1.7.0
* Updated dependent libraries.
* Minimum java version is 1.8.

## 1.6.1
* Fixes to depedency paths in OSGi imported packages.

## 1.6.0
* As of this version dependencies are bundled making this package a valid OSGi bundle.

## 1.5.1
* Properly close response body for requests where body is unused.
* Migrate internals to OkHttp3 (minimum version required is 3.3.0).

## 1.5.0
* Retry failed requests by default.

## 1.4.1
* Bugfix: Avoid java.security.cert.CertPathValidatorException on Android.

## 1.4.0
* Support for HTTP proxies.
