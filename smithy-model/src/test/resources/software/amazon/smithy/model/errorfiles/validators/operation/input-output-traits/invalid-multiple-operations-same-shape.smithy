$version: "1.0"
namespace smithy.example

operation GetFoo {
    input: GetFooInput,
    output: GetFooOutput
}

operation GetFoo2 {
    input: GetFooInput,
    output: GetFooOutput
}

@input
structure GetFooInput {}

@output
structure GetFooOutput {}