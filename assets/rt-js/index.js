import * as wasmGlue from './libwasm/native_rt.js';

(async () => {
  await wasmGlue.__wbg_init();
})();

export const rt = wasmGlue;

export const id = function(x) {
  console.log(JSON.stringify(x));
  return x;
};

