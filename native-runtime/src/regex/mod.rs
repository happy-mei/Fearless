use crate::strings::FearlessStr;
use jni::objects::{JByteArray, JByteBuffer, JClass};
use jni::sys::{jboolean, jobject};
use jni::JNIEnv;
use regex::Regex;


/// # Safety
/// The Fearless string is valid UTF-8.
#[no_mangle]
pub unsafe extern "system" fn Java_rt_NativeRuntime_compileRegexPattern<'local>(mut env: JNIEnv<'local>, _class: JClass<'local>, utf8_regex_str: JByteArray<'local>) -> jobject {
    let str = FearlessStr::new(&mut env, &utf8_regex_str);
    match Regex::new(str.as_str()) {
        Ok(pattern) => {
            let pattern = Box::new(pattern);
            let size = size_of_val(&pattern);
            let pattern = Box::into_raw(pattern);
            let buffer = env.new_direct_byte_buffer(pattern as *mut _ as *mut u8, size).unwrap();
            buffer.into_raw()
        },
        Err(err) => todo!()
    }
}

/// # Safety
/// The byte buffer refers to a pattern allocated by `Java_rt_NativeRuntime_compileRegexPattern`.
/// The contents of the byte buffer are never dereferenced after calling this method.
#[no_mangle]
pub unsafe extern "system" fn Java_rt_NativeRuntime_dropRegexPattern<'local>(env: JNIEnv<'local>, _class: JClass<'local>, pattern: JByteBuffer<'local>) {
    let raw_addr = env.get_direct_buffer_address(&pattern).unwrap();
    // let raw_size = env.get_direct_buffer_capacity(&pattern);
    drop(Box::from_raw(raw_addr as *mut Regex));
}

#[no_mangle]
pub unsafe extern "system" fn Java_rt_NativeRuntime_doesRegexMatch<'local>(mut env: JNIEnv<'local>, _class: JClass<'local>, pattern: JByteBuffer<'local>, str: JByteArray<'local>) -> jboolean {
    let regex = from_java(&env, pattern);
    let str = FearlessStr::new(&mut env, &str);
    regex.is_match(str.as_str()).into()
}

unsafe fn from_java<'a,'local>(env: &JNIEnv<'local>, pattern: JByteBuffer<'a>) -> &'a Regex {
    let raw_addr = env.get_direct_buffer_address(&pattern).unwrap() as *mut Regex;
    &*raw_addr
}
