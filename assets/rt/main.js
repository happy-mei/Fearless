import initWasm, * as wasm from './libwasm/';

// WASM initialization state
let wasmInitialized = false;

export async function initializeRuntime() {
  if (!wasmInitialized) {
    await initWasm();
    wasmInitialized = true;
  }
}

// WASM-backed implementations
export const Native = {
  hashString: wasm.hash_string,
  normalizeString: wasm.normalise_string,
  indexString: wasm.index_string,
  compileRegex: wasm.compile_regex,
  testRegex: wasm.does_regex_match
};