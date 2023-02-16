package codegen;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import failure.CompileError;
import main.Main;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import parser.Parser;
import program.inference.InferBodies;
import utils.Err;
import wellFormedness.WellFormednessFullShortCircuitVisitor;
import wellFormedness.WellFormednessShortCircuitVisitor;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class TestMIR {
  void ok(String expected, String... content){
    assert content.length > 0;
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    var ps = Arrays.stream(content)
      .map(code -> new Parser(Path.of("Dummy"+pi.getAndIncrement()+".fear"), code))
      .toList();
    var p = Parser.parseAll(ps);
    new WellFormednessFullShortCircuitVisitor().visitProgram(p).ifPresent(err->{ throw err; });
    var inferredSigs = p.inferSignaturesToCore();
    var inferred = new InferBodies(inferredSigs).inferAll(p);
    new WellFormednessShortCircuitVisitor().visitProgram(inferred);
    inferred.typeCheck();
    var mir = new MIRInjectionVisitor(inferred).visitProgram();
    var toJson = new ObjectMapper().registerModule(new Jdk8Module().configureAbsentsAsNulls(true));
    try {
      Err.strCmpFormat(expected, toJson.writeValueAsString(mir));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
  void fail(String expectedErr, String... content){
    assert content.length > 0;
    Main.resetAll();
    AtomicInteger pi = new AtomicInteger();
    var ps = Arrays.stream(content)
      .map(code -> new Parser(Path.of("Dummy"+pi.getAndIncrement()+".fear"), code))
      .toList();
    var p = Parser.parseAll(ps);
    new WellFormednessFullShortCircuitVisitor().visitProgram(p).ifPresent(err->{ throw err; });
    var inferredSigs = p.inferSignaturesToCore();
    var inferred = new InferBodies(inferredSigs).inferAll(p);
    inferred.typeCheck();
    var toJson = new ObjectMapper();
    try {
      var mir = toJson.writeValueAsString(new MIRInjectionVisitor(inferred).visitProgram());
      Assertions.fail("Did not fail, got:\n" + mir);
    } catch (CompileError e) {
      Err.strCmp(expectedErr, e.toString());
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @Test void emptyProgram() { ok("""
    {"pkgs":{}}
    """, """
    package test
    """);}

  @Test void simpleProgram() { ok("""
{
  "pkgs": {
    "test": [
      {
        "name": "test.Bar_0",
        "gens": [],
        "its": [
          "test.Bar_0",
          "test.Baz_1"
        ],
        "meths": [
          {
            "name": "loop",
            "mdf": "imm",
            "gens": [],
            "xs": [],
            "rt": "test.Baz_1<test.Bar_0>",
            "body": {
              "op": "MIR$MCall",
              "recv": {
                "op": "MIR$X",
                "mdf": "imm",
                "name": "this",
                "type": "test.Bar_0"
              },
              "name": "loop",
              "args": []
            },
            "abs": false
          },
          {
            "name": "$35",
            "mdf": "imm",
            "gens": [],
            "xs": [],
            "rt": "test.Foo_0",
            "body": {
              "op": "MIR$Lambda",
              "mdf": "imm",
              "freshName": "test.Fear7$36_0",
              "selfName": "fear0$",
              "its": [
                "test.Foo_0"
              ],
              "captures": [],
              "meths": []
            },
            "abs": false
          }
        ]
      },
      {
        "name": "test.Foo_0",
        "gens": [],
        "its": [
          "test.Foo_0"
        ],
        "meths": []
      },
      {
        "name": "test.Ok_0",
        "gens": [],
        "its": [
          "test.Ok_0"
        ],
        "meths": [
          {
            "name": "$35",
            "mdf": "imm",
            "gens": [],
            "xs": [],
            "rt": "test.Ok_0",
            "body": null,
            "abs": true
          }
        ]
      },
      {
        "name": "test.Baz_1",
        "gens": [
          "X"
        ],
        "its": [
          "test.Baz_1"
        ],
        "meths": [
          {
            "name": "$35",
            "mdf": "imm",
            "gens": [],
            "xs": [],
            "rt": "X",
            "body": null,
            "abs": true
          }
        ]
      },
      {
        "name": "test.Yo_0",
        "gens": [],
        "its": [
          "test.Yo_0"
        ],
        "meths": [
          {
            "name": "lm",
            "mdf": "imm",
            "gens": [],
            "xs": [],
            "rt": "test.Ok_0",
            "body": {
              "op": "MIR$Lambda",
              "mdf": "imm",
              "freshName": "test.Fear8$36_0",
              "selfName": "ok",
              "its": [
                "test.Ok_0"
              ],
              "captures": [],
              "meths": [
                {
                  "name": "$35",
                  "mdf": "imm",
                  "gens": [],
                  "xs": [],
                  "rt": "test.Ok_0",
                  "body": {
                    "op": "MIR$MCall",
                    "recv": {
                      "op": "MIR$X",
                      "mdf": "imm",
                      "name": "ok",
                      "type": "Fear8$36_0"
                    },
                    "name": "$35",
                    "args": []
                  },
                  "abs": false
                }
              ]
            },
            "abs": false
          }
        ]
      },
      {
        "name": "test.Fear7$36_0",
        "gens": [],
        "its": [
          "test.Foo_0"
        ],
        "meths": []
      },
      {
        "name": "test.Fear8$36_0",
        "gens": [],
        "its": [
          "test.Ok_0"
        ],
        "meths": []
      }
    ]
  }
}
    """, """
    package test
    Baz[X]:{ #: X }
    Bar:Baz[Foo]{ # -> Foo, .loop: Baz[Bar] -> this.loop }
    Ok:{ #: Ok }
    Yo:{ .lm: Ok -> {'ok ok# } }
    Foo:{}
    """);}

  /*
  TODO breaks inference:
  package test
  Baz[X]:{ #: X }
  Bar:Baz[Foo]{ # -> Foo, .loop: Baz[Bar] -> this.loop }
  Yo:{ .lm: Bar -> { .loop -> { Bar } } }


  also
  package test
  Baz[X]:{ #: X }
  Bar:Baz[Foo]{ # -> Foo{ .boo: Foo -> Foo }, .loop: Baz[Bar] -> this.loop }
  Foo:{}
   */
}
