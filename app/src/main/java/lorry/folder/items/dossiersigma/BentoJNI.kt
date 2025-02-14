package lorry.folder.items.dossiersigma

object BentoJNI {
    init {
        System.loadLibrary("c++_shared")
        System.loadLibrary("AddTag")
    }

    external fun AddTag_C(file: String, arg: String, removeFirst: Int): String
}
