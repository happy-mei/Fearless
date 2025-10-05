import { rt$$NativeRuntime } from "../../../assets/rt-js/NativeRuntime.js";
import assert from "node:assert/strict";

rt$$NativeRuntime.ensureWasm();

console.log("Running NativeRuntime WASM tests…");

// --- hashString ---
{
  const s = "Hello 🌍";
  const h1 = rt$$NativeRuntime.hashString(s);
  const h2 = rt$$NativeRuntime.hashString(s);
  // console.log("hashString:", h1);
  assert.equal(h1, h2, "hashString must be deterministic");
}

// --- normaliseString ---
{
  const composed = "Å";   // U+00C5
  const decomposed = "Å"; // U+0041 + U+030A
  const norm = rt$$NativeRuntime.normaliseString(decomposed);
  // console.log("normaliseString:", norm);
  assert.equal(norm, composed, "normaliseString should canonicalize decomposed forms");
}

// --- indexString ---
{
  const input = "👩🏽‍💻abc";
  const indices = rt$$NativeRuntime.indexString(input);
  // console.log("indexString:", indices);
  assert.ok(indices instanceof Int32Array, "indexString must return an Int32Array");
  assert.ok(indices.length > 1, "indexString must return at least one grapheme index");
  assert.equal(indices[0], 0, "First grapheme should start at 0");
}

// --- compileRegex and doesRegexMatch ---
{
  const pattern = "^[a-z]+$";
  const compiled = rt$$NativeRuntime.compileRegex(pattern);
  assert.ok(compiled, "compileRegex should return a valid object/value");

  assert.equal(rt$$NativeRuntime.doesRegexMatch(pattern, "hello"), true);
  assert.equal(rt$$NativeRuntime.doesRegexMatch(pattern, "HELLO"), false);
  assert.equal(rt$$NativeRuntime.doesRegexMatch(pattern, "abc123"), false);
  assert.equal(rt$$NativeRuntime.doesRegexMatch(pattern, "abc"), true);

  const numPattern = "^[0-9]+$";
  assert.equal(rt$$NativeRuntime.doesRegexMatch(numPattern, "12345"), true);
  assert.equal(rt$$NativeRuntime.doesRegexMatch(numPattern, "123a"), false);
}

console.log("✅ All NativeRuntime WASM assertions passed.\n");
