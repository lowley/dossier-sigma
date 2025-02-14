/* Bento4Wrapper.h */
#ifndef _Included_lorry_folder_items_dossiersigma_Bento4Wrapper
#define _Included_lorry_folder_items_dossiersigma_Bento4Wrapper

#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jboolean JNICALL Java_lorry_folder_items_dossiersigma_Bento4Wrapper_editMp4Metadata
        (JNIEnv *, jclass, jstring, jstring);

#ifdef __cplusplus
}
#endif
#endif