$version: "2.0"

namespace smithy.example

structure Foo {
    @default(foo: "bar")
    bar: StringMap
}

map StringMap {
    key: String
    value: String
}
