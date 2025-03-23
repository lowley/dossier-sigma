package lorry.folder.items.dossiersigma

object BentoJNI {
    init {
        try {
            println("🔄 Tentative de chargement de bento4...")
            System.loadLibrary("bento4")
            println("✅ Bibliothèque bento4 chargée avec succès !")
        } catch (e: UnsatisfiedLinkError) {
            println("❌ Erreur JNI : " + e.message)
        }
    }

    external fun AddTagCC(file: String, arg: String, removeFirst: Int): String

    external fun ExtractTagCC(file: String, image: String): String
}
