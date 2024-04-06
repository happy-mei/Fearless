use std::io;
use std::io::Write;
use jni::JNIEnv;
use jni::objects::{JByteArray, JClass, JObject};
use jni::sys::{jint, jintArray, jsize};
use unicode_segmentation::UnicodeSegmentation;

use crate::strings::FearlessStr;

mod strings;

#[no_mangle]
pub extern "system" fn Java_rt_NativeRuntime_indexString<'local>(mut env: JNIEnv<'local>, _class: JClass<'local>, utf8_str: JByteArray<'local>) -> jintArray {
    let graphemes = {
        let f_str = FearlessStr::new(&mut env, &utf8_str);
        let str = match f_str.as_str() {
            Ok(str) => str,
            Err(err) => {
                env.throw_new("java/lang/RuntimeException", format!("{}", err)).unwrap();
                return JObject::null().into_raw();
            }
        };

        // Java ByteBuffers and byte[] must be indexable by a signed integer to be created,
        // so it is impossible to ever overflow when casting the usize to an i32
        str.grapheme_indices(true)
            .map(|(idx, _grapheme)| idx as jint)
            .collect::<Vec<_>>()
    };

    let res = env.new_int_array(graphemes.len() as jsize).unwrap();
    env.set_int_array_region(&res, 0, &graphemes).unwrap();
    res.into_raw()
}

#[no_mangle]
pub extern "system" fn Java_rt_NativeRuntime_print<'local>(env: JNIEnv<'local>, _class: JClass<'local>, utf8_str: JByteArray<'local>) {
    print(env, utf8_str, false, io::stdout().lock());
}
#[no_mangle]
pub extern "system" fn Java_rt_NativeRuntime_println<'local>(env: JNIEnv<'local>, _class: JClass<'local>, utf8_str: JByteArray<'local>) {
    print(env, utf8_str, true, io::stdout().lock());
}
#[no_mangle]
pub extern "system" fn Java_rt_NativeRuntime_printErr<'local>(env: JNIEnv<'local>, _class: JClass<'local>, utf8_str: JByteArray<'local>) {
    print(env, utf8_str, false, io::stderr().lock());
}
#[no_mangle]
pub extern "system" fn Java_rt_NativeRuntime_printlnErr<'local>(env: JNIEnv<'local>, _class: JClass<'local>, utf8_str: JByteArray<'local>) {
    print(env, utf8_str, true, io::stderr().lock());
}

fn print<'local, B: Write>(mut env: JNIEnv<'local>, utf8_str: JByteArray<'local>, append_newline: bool, mut buffer: B) {
    let f_str = FearlessStr::new(&mut env, &utf8_str);
    buffer.write_all(f_str.as_bytes()).unwrap();
    if append_newline { buffer.write_all(b"\n").unwrap(); }
    buffer.flush().unwrap();
}

#[cfg(test)]
mod tests {
    #[test]
    fn it_works() {
    }
}
