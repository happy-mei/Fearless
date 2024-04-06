use std::marker::PhantomData;
use std::slice;
use std::str::Utf8Error;
use jni::JNIEnv;

use jni::objects::{AutoElements, JByteArray, ReleaseMode};
use jni::sys::jbyte;

#[repr(transparent)]
pub struct FearlessStr<'a, 'local, 'array_local, 'array> {
    array_ref: AutoElements<'local, 'array_local, 'array, jbyte>,
    _keep_alive: PhantomData<&'a ()>,
}
impl<'a, 'local, 'array_local, 'array> FearlessStr<'a, 'local, 'array_local, 'array> {
    pub fn new(env: &mut JNIEnv<'local>, utf8_str: &'array JByteArray<'array_local>) -> FearlessStr<'a, 'local, 'array_local, 'array> {
        // Safety: Lifetimes bind the data here. Making this critical would be safe too,
        // but we don't need to do that unless this is becoming a perf issue (making it critical
        // has stalling risks on other threads because it locks the allocator)
        let array_ref = unsafe { env.get_array_elements(utf8_str, ReleaseMode::NoCopyBack).unwrap() };
        Self { array_ref, _keep_alive: PhantomData }
    }
    pub fn as_str(&'a self) -> Result<&'a str, Utf8Error> {
        // Safety:
        // - The JNI guarantees that there is data here with the provided length.
        // - i8 and u8 are safely interchangeable for the bytes here
        let raw_str = self.as_bytes();
        std::str::from_utf8(raw_str)
    }
    fn as_bytes(&'a self) -> &[u8] {
        // Safety:
        // - The JNI guarantees that there is data here with the provided length.
        // - i8 and u8 are safely interchangeable for the bytes here
        unsafe {
            let java_bytes: &[i8] = slice::from_raw_parts(self.array_ref.as_ptr(), self.array_ref.len());
            std::mem::transmute(java_bytes)
        }
    }
}
