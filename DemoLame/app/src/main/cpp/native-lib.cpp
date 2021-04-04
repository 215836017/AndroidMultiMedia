#include <jni.h>
#include <string>

#include "libmp3lame/lame.h"

extern "C"
JNIEXPORT jstring JNICALL
Java_com_cakes_demolame_jni_JniLameUtil_getLameVersion(JNIEnv *env, jclass clazz) {
    return (*env).NewStringUTF(get_lame_version());
}


extern "C"
JNIEXPORT void JNICALL
Java_com_cakes_demolame_jni_JniLameUtil_startEncode(JNIEnv *env, jclass clazz, jstring wav_path,
                                                    jstring mp3_string) {
    // TODO: implement startEncode()
}