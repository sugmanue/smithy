$version: "2.0"

namespace smithy.example

structure Foo {
    @default(0)
    a: Document

    @default(true)
    b: Document

    @default("hello")
    c: Document

    @default([])
    d: Document

    @default({})
    e: Document

    @default([1])
    f: Document

    @default(foo: true)
    g: Document
}
