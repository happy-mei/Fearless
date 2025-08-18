#!/bin/sh
# WASM Runtime Build
echo "Building WASM runtime..."
cd native-runtime
wasm-pack build --target web --features wasm
mkdir -p ../artefacts/rt/libwasm
cp pkg/* ../artefacts/rt/libwasm/
cd ..