package lorry.folder.items.dossiersigma

class Bento4Wrapper {
    companion object {
        // Charge la bibliothèque partagée Bento4
        init {
            System.loadLibrary("bento4")
        }

        /**
         * Méthode JNI pour modifier les métadonnées d'un MP4 (ajouter une icône).
         * @param mp4Path Chemin du fichier MP4
         * @param iconPath Chemin de l'image PNG à ajouter
         * @return true si l'opération réussit, false sinon
         */
        @JvmStatic
        external fun ditMp4Metadata(mp4Path: String, iconPath: String): Boolean
    }
}
