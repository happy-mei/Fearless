use crate::strings::FearlessStr;
use jni::objects::{JByteArray, JClass};
use jni::sys::{jboolean, jlong};
use jni::JNIEnv;
use regex::Regex;


/// # Safety
/// The Fearless string is valid UTF-8.
#[no_mangle]
pub unsafe extern "system" fn Java_rt_NativeRuntime_compileRegexPattern<'local>(mut env: JNIEnv<'local>, _class: JClass<'local>, utf8_regex_str: JByteArray<'local>) -> jlong {
    let str = FearlessStr::new(&mut env, &utf8_regex_str);
    match Regex::new(str.as_str()) {
        Ok(pattern) => {
            let pattern = Box::new(pattern);
            let pattern = Box::into_raw(pattern);
            pattern as jlong
        },
        Err(err) => {
            env.throw_new("rt/NativeRuntime$Regex$InvalidRegexError", format!("{}", err)).unwrap();
            0
        }
    }
}

/// # Safety
/// The byte buffer refers to a pattern allocated by `Java_rt_NativeRuntime_compileRegexPattern`.
/// The contents of the byte buffer are never dereferenced after calling this method.
#[no_mangle]
pub unsafe extern "system" fn Java_rt_NativeRuntime_dropRegexPattern<'local>(_env: JNIEnv<'local>, _class: JClass<'local>, pattern: jlong) {
    drop(Box::from_raw(pattern as *mut Regex));
}

#[no_mangle]
pub unsafe extern "system" fn Java_rt_NativeRuntime_doesRegexMatch<'local>(mut env: JNIEnv<'local>, _class: JClass<'local>, pattern: jlong, str: JByteArray<'local>) -> jboolean {
    let regex = from_java(pattern);
    let str = FearlessStr::new(&mut env, &str);
    regex.is_match(str.as_str()).into()
}

unsafe fn from_java<'a>(pattern: jlong) -> &'a Regex {
    let ptr = pattern as *mut Regex;
    &*ptr
}
