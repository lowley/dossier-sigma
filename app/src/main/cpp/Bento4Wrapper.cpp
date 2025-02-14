#include <jni.h>
#include <string>
#include <iostream>
#include <cstdlib>

extern "C"
JNIEXPORT jboolean JNICALL
Java_lorry_folder_items_dossiersigma_Bento4Wrapper_editMp4Metadata(
        JNIEnv *env,
        jclass /* this */,
        jstring mp4Path,
        jstring iconPath) {

    const char *mp4_file = env->GetStringUTFChars(mp4Path, nullptr);
    const char *icon_file = env->GetStringUTFChars(iconPath, nullptr);

    // Commande Bento4 : Ajouter une miniature (Cover Art) au MP4
    std::string command = "mp4tag --artwork " + std::string(icon_file) + " " + std::string(mp4_file);
    int result = std::system(command.c_str());

    env->ReleaseStringUTFChars(mp4Path, mp4_file);
    env->ReleaseStringUTFChars(iconPath, icon_file);

    if (result == 0) {
        std::cout << "✅ Miniature MP4 ajoutée avec succès !" << std::endl;
        return JNI_TRUE;
    } else {
        std::cerr << "❌ Erreur lors de l'ajout de la miniature MP4." << std::endl;
        return JNI_FALSE;
    }
}
