licenses(["notice"])  # Apache 2.0

load(
    "//:transpiler/javatests/com/google/j2cl/transpiler/readable/readable_example.bzl",
    "readable_example",
)

# Creates verification target
readable_example(
    srcs = glob(["*.java"]),
    defs = ["--allow_method_call_decomposing"],
    deps = [
        "//third_party:gwt-jsinterop-annotations-j2cl",
    ],
)
