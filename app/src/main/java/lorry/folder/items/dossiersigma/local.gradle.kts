package lorry.folder.items.dossiersigma

// local.gradle.kts — NON commiter (ajoute à .gitignore)
// Exemple : override manifestPlaceholders et applicationIdSuffix pour dev local
android {
    defaultConfig {
        manifestPlaceholders["applicationId"] = "lorry.folder.items.dossiersigma.onair"
        "applicationId" = "lorry.folder.items.dossiersigma.onair"
    }

//    productFlavors {
//        create("master") {
//            // exemple : override juste pour ton dev local (utile pour debug)
//             applicationIdSuffix = ".master" // normalement dans le build principal
//        }
//        // tu peux définir des propriétés custom
//    }

    // Ou définir une propriété BuildConfig
//    buildTypes {
//        getByName("debug") {
//            // Exemple : définir un buildConfigField local (visible dans BuildConfig)
//            buildConfigField("String", "LOCAL_DEV_FLAG", "\"true\"")
//        }
//    }
}