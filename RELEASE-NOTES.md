# Polyglot Release Notes

Quick links:

* [Releases page](https://github.com/dinowernli/polyglot/releases)

## Upcoming release

* Added support for grpc server reflection
* Added support for custom metadata

## 1.3.0

* Upgraded to grpc 1.4.0.
* Added support for client certificates to TLS.

## 1.2.0

* Upgraded to grpc 1.0.0.
* Removed the separate per-platform binaries, the same binary can now be used on all platforms.

## 1.1.0

* Added a new command, called `list` as well as a documentation section in the README. The default command is `call`, which makes an rpc call to a remote endpoint.
* Added support for rpcs which contain multiple requests (`client_streaming` and `bidi_streaming`).
* Response messages are now printed to `stdout` rather than the logs. This makes piping the results to files easier.
* The `--help` output now no longer contains a stack trace.

## 1.0.0

* Initial release.
