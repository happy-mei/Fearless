use jni::JNIEnv;
use jni::objects::{JClass, JObject, JString};
use jni::sys::{jobjectArray, jsize};
use unicode_segmentation::UnicodeSegmentation;

#[no_mangle]
pub extern "system" fn Java_rt_FearlessUnicode_parse<'local>(mut env: JNIEnv<'local>, _class: JClass<'local>, input: JString<'local>) -> jobjectArray {
    let input: String = env.get_string(&input).expect("Failed to parse Java String").into();
    let graphemes = input.graphemes(true).collect::<Vec<_>>();
    if (jsize::MAX as usize) < graphemes.len() {
        env.throw_new("java/lang/RuntimeException", "Cannot create strings that require more than 32-bits to index.").expect("Failed to throw exception");
        return JObject::null().into_raw();
    }

    let res = env.new_object_array(graphemes.len() as jsize, "[B", JObject::null()).expect("Failed to create output array");
    for (n, grapheme) in graphemes.into_iter().enumerate() {
        // Safety: u8 and i8 are interchangeable here
        let bytes: &[i8] = unsafe { std::mem::transmute(grapheme.as_bytes()) };
        if (jsize::MAX as usize) < bytes.len() {
            env.throw_new("java/lang/RuntimeException", "Cannot create a grapheme cluster that requires more than 32-bits to index.").expect("Failed to throw exception");
            return JObject::null().into_raw();
        }
        let byte_arr = env.new_byte_array(bytes.len() as jsize).expect("Failed to allocate byte array");
        env.set_byte_array_region(&byte_arr, 0, bytes).expect("Failed to fill byte array");
        env.set_object_array_element(&res, n as jsize, &byte_arr).expect("Failed to fill outer array");
    }
    res.into_raw()
}

#[cfg(test)]
mod tests {
    #[test]
    fn it_works() {
    }
}
