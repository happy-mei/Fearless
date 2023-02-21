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
        "name": {
          "name": "test.Bar",
          "gen": 0
        },
        "gens": [],
        "its": [
          {
            "name": {
              "name": "test.Baz",
              "gen": 1
            },
            "ts": [
              {
                "mdf": "imm",
                "rt": {
                  "name": {
                    "name": "test.Foo",
                    "gen": 0
                  },
                  "ts": []
                },
                "it": true
              }
            ]
          }
        ],
        "meths": [
          {
            "name": {
              "name": ".loop",
              "num": 0
            },
            "mdf": "imm",
            "gens": [],
            "xs": [],
            "rt": {
              "mdf": "imm",
              "rt": {
                "name": {
                  "name": "test.Baz",
                  "gen": 1
                },
                "ts": [
                  {
                    "mdf": "imm",
                    "rt": {
                      "name": {
                        "name": "test.Bar",
                        "gen": 0
                      },
                      "ts": []
                    },
                    "it": true
                  }
                ]
              },
              "it": true
            },
            "body": {
              "op": "MIR$MCall",
              "recv": {
                "op": "MIR$X",
                "name": "this",
                "type": {
                  "mdf": "imm",
                  "rt": {
                    "name": {
                      "name": "test.Bar",
                      "gen": 0
                    },
                    "ts": []
                  },
                  "it": true
                }
              },
              "name": {
                "name": ".loop",
                "num": 0
              },
              "args": []
            },
            "abs": false
          },
          {
            "name": {
              "name": "#",
              "num": 0
            },
            "mdf": "imm",
            "gens": [],
            "xs": [],
            "rt": {
              "mdf": "imm",
              "rt": {
                "name": {
                  "name": "test.Foo",
                  "gen": 0
                },
                "ts": []
              },
              "it": true
            },
            "body": {
              "op": "MIR$Lambda",
              "mdf": "imm",
              "freshName": {
                "name": "test.Foo",
                "gen": 0
              },
              "selfName": "fear0$",
              "its": [],
              "captures": [],
              "meths": []
            },
            "abs": false
          }
        ]
      },
      {
        "name": {
          "name": "test.Foo",
          "gen": 0
        },
        "gens": [],
        "its": [],
        "meths": []
      },
      {
        "name": {
          "name": "test.Ok",
          "gen": 0
        },
        "gens": [],
        "its": [],
        "meths": [
          {
            "name": {
              "name": "#",
              "num": 0
            },
            "mdf": "imm",
            "gens": [],
            "xs": [],
            "rt": {
              "mdf": "imm",
              "rt": {
                "name": {
                  "name": "test.Ok",
                  "gen": 0
                },
                "ts": []
              },
              "it": true
            },
            "body": null,
            "abs": true
          }
        ]
      },
      {
        "name": {
          "name": "test.Baz",
          "gen": 1
        },
        "gens": [
          {
            "name": "X"
          }
        ],
        "its": [],
        "meths": [
          {
            "name": {
              "name": "#",
              "num": 0
            },
            "mdf": "imm",
            "gens": [],
            "xs": [],
            "rt": {
              "mdf": "imm",
              "rt": {
                "name": "X"
              },
              "it": false
            },
            "body": null,
            "abs": true
          }
        ]
      },
      {
        "name": {
          "name": "test.Yo",
          "gen": 0
        },
        "gens": [],
        "its": [],
        "meths": [
          {
            "name": {
              "name": ".lm",
              "num": 0
            },
            "mdf": "imm",
            "gens": [],
            "xs": [],
            "rt": {
              "mdf": "imm",
              "rt": {
                "name": {
                  "name": "test.Ok",
                  "gen": 0
                },
                "ts": []
              },
              "it": true
            },
            "body": {
              "op": "MIR$Lambda",
              "mdf": "imm",
              "freshName": {
                "name": "test.Ok",
                "gen": 0
              },
              "selfName": "ok",
              "its": [],
              "captures": [],
              "meths": [
                {
                  "name": {
                    "name": "#",
                    "num": 0
                  },
                  "mdf": "imm",
                  "gens": [],
                  "xs": [],
                  "rt": {
                    "mdf": "imm",
                    "rt": {
                      "name": {
                        "name": "test.Ok",
                        "gen": 0
                      },
                      "ts": []
                    },
                    "it": true
                  },
                  "body": {
                    "op": "MIR$MCall",
                    "recv": {
                      "op": "MIR$X",
                      "name": "ok",
                      "type": {
                        "mdf": "imm",
                        "rt": {
                          "name": {
                            "name": "test.Ok",
                            "gen": 0
                          },
                          "ts": []
                        },
                        "it": true
                      }
                    },
                    "name": {
                      "name": "#",
                      "num": 0
                    },
                    "args": []
                  },
                  "abs": false
                }
              ]
            },
            "abs": false
          }
        ]
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
