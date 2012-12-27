jdbcdslog
---------

This is a forked version of jdbcdslog-exp(https://github.com/jdbcdslog/jdbcdslog) that:

* supports logging via pluggable event handler, not only slf4j.
* supports logging to Fluentd
* includes a plugin for Play framework 2.0(http://www.playframework.org/).

It may support the same functionality that the original jdbcdslog supports.
See the documentation for the original jdbcdslog(http://code.google.com/p/jdbcdslog/) for more info.

= Artifacts

"jp.furyu.jdbcdslog" %% "jdbcdslog-core" % "0.2-SNAPSHOT"
"jp.furyu.jdbcdslog" %% "jdbcdslog-fluent" % "0.2-SNAPSHOT"
"jp.furyu.play2" %% "play2-jdbcdslog" % "0.2-SNAPSHOT"
