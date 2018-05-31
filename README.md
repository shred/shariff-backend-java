# Shariff Java Backend ![build status](https://shredzone.org/badge/shariff-backend-java.svg) ![maven central](https://shredzone.org/maven-central/org.shredzone.shariff/backend/badge.svg)

[Shariff](https://github.com/heiseonline/shariff) is used to determine how often a page is shared in social media, but without generating requests from the displaying page to the social sites.

This document describes the Java backend. It is some kind of a port of the [offical Shariff PHP backend](https://github.com/heiseonline/shariff-backend-php).

This Shariff backend is not a part of the official backends by Heise Online!

## Features

* Easy to use Java servlet
* Supports `addthis`, `facebook`, `flattr`, `linkedin`, `pinterest`, `reddit`, `stumbleupon`, `vk`, `xing`
* Parallel counter fetching to minimize response time
* Comes with a simple caching mechanism that can be replaced by other cache solutions like [Ehcache](http://ehcache.org)
* Built with maven, package is available at Maven Central
* Uses `slf4j`, for log output just add a binding to your classpath
* Requires Java 8 or higher (Java 7 up to version 1.1)

## Installing the Shariff backend on your own server

Just add Shariff (`org.shredzone.shariff:backend:1.4`) to your maven or gradle dependencies, or copy the `shariff.jar`, [`json.jar`](https://mvnrepository.com/artifact/org.json/json) and [`slf4j`](https://www.slf4j.org/download.html) files to your project's lib folder. Then add the Shariff servlet to your `web.xml`:

```xml
<servlet>
    <servlet-name>shariff</servlet-name>
    <servlet-class>org.shredzone.shariff.ShariffServlet</servlet-class>
</servlet>
<servlet-mapping>
    <servlet-name>shariff</servlet-name>
    <url-pattern>/shariff/</url-pattern>
</servlet-mapping>
```

Use `init-param` to configure the servlet:

```xml
<servlet>
    <servlet-name>shariff</servlet-name>
    <servlet-class>org.shredzone.shariff.ShariffServlet</servlet-class>
    <init-param>
        <param-name>targets</param-name>
        <param-value>flattr,facebook</param-value>
    </init-param>
</servlet>
```

The following configuration options are available:

| Key         | Description |
|-------------|-------------|
| `host `     | Regular expression of acceptable hosts (e.g. "^(.*\\.)?example\\.com$"). If unset, only the host of the servlet is accepted. It's recommended to keep foreign websites from using your Shariff server. |
| `cacheSize` | Maximum number of urls to be cached in memory. Default is 1000. |
| `cacheTimeToLiveMs` | Maximum time urls are cached, in ms. Default is 1 minute. |
| `targets`   | List of services to be enabled (see [Features](#features)). Case sensitive. Services must be separated by comma. Default is all available services. |
| `threads`   | Number of fetcher threads. Defaults to number of active targets. |
| `facebook.id` | The app ID of your Facebook application. |
| `facebook.secret` | The app secret of your Facebook application. |

Note that you _must_ set up `facebook.id` and `facebook.secret` to retrieve a valid Facebook share counter. Facebook does not offer an anonymous way any more, at least none I am aware of.

## Testing your installation

If the backend runs under `http://example.com/shariff/`, calling the URL `http://example.com/shariff/?url=http%3A%2F%2Fwww.example.com` should return a JSON structure with numbers in it, e.g.:

```json
{"facebook":1452,"flattr":23}
```

You can also invoke `ShariffBackend` directly. Pass in the Facebook credentials via system properties `facebook.id` and `facebook.secret` if necessary. It returns the share counters for the given URLs on the command line:

```
java -cp backend.jar:json.jar:slf4j-api.jar:slf4j-simple.jar \
  -Dfacebook.id=myAppId -Dfacebook.secret=myAppSecret \
  org.shredzone.shariff.ShariffBackend \
  http://www.heise.de
```

## Shariff Servlet

You can extend the `ShariffServlet` class and override its protected methods if you need more control about fetching or caching the count data.

To replace the caching mechanism (e.g. by [Ehcache](http://ehcache.org)), override the `getCountsCached()` method.

For reading and normalizing the url parameter, override the `getUrl()` method.

For an individual test whether the given url contains a valid host or not, override the `isValidHost()` method.

Use this snippet to set the Facebook credentials programmatically:

```
Facebook facebook = getBackend().getTarget(Facebook.class);
if (facebook != null) {
    facebook.setSecret(yourClientId, yourClientSecret);
}
```

## License

shariff-backend-java is released under the terms of the Apache License 2.0.
