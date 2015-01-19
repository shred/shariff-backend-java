Shariff Java Backend
====================

[Shariff](https://github.com/heiseonline/shariff) is used to determine how often a page is shared in social media, but without generating requests from the displaying page to the social sites.

![Shariff Logo © 2014 Heise Zeitschriften Verlag](http://www.heise.de/icons/ho/shariff-logo.png)

This document describes the Java backend. The following backends are also available:

* [shariff-backend-node](https://github.com/heiseonline/shariff-backend-node)
* [shariff-backend-perl](https://github.com/heiseonline/shariff-backend-perl)
* [shariff-backend-php](https://github.com/heiseonline/shariff-backend-php)

This Shariff backend is not a part of the official backends by Heise Online! It bases on [shariff-backend-php](https://github.com/heiseonline/shariff-backend-php).

![build status](http://jenkins.shredzone.net/buildStatus/icon?job=shariff-backend-java)


Installing the Shariff backend on your own server
-------------------------------------------------

Add Shariff (`org.shredzone.shariff:backend:1.0`) to your maven or gradle dependencies, or copy the `shariff.jar` and [`json.jar`](http://www.json.org/java/index.html) to your project's lib folder. Then add the Shariff servlet to your `web.xml`:

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
        <param-value>googleplus,twitter</param-value>
    </init-param>
</servlet>
```

The following configuration options are available:

| Key         | Description |
|-------------|-------------|
| `host `     | Regular expression of acceptable hosts (e.g. "^(.*\\.)?example\\.com$"). If unset, only the host of the servlet is accepted.|
| `cacheSize` | Maximum number of urls to be cached in memory. Default is 1000. |
| `cacheTimeToLiveMs` | Maximum time urls are cached, in ms. Default is 1 minute. |
| `targets`   | List of services to be enabled. Available: `facebook`, `flattr`, `googleplus`, `linkedin`, `pinterest`, `reddit`, `stumbleupon`, `twitter`, `xing`. Case sensitive. Services must be separated by comma. Default is all available services. |
| `threads`   | Number of fetcher threads. Defaults to number of active targets. |


Testing your installation
-------------------------

If the backend runs under `http://example.com/my-shariff-backend/`, calling the URL `http://example.com/my-shariff-backend/?url=http%3A%2F%2Fwww.example.com` should return a JSON structure with numbers in it, e.g.:

```json
{"facebook":1452,"twitter":404,"googleplus":23}
```


Shariff Servlet
---------------

You can extend the `ShariffServlet` class and override its protected methods if you need more control about fetching or caching the count data.

To replace the caching mechanism (e.g. by EHCache), override the `getCountsCached()` method.

For reading and normalizing the url parameter, override the `getUrl()` method.

For an individual test whether the given url contains a valid host or not, override the `isValidHost()` method.


License
-------

shariff-backend-java is released under the terms of the Apache License 2.0.
