package lorry.folder.items.dossiersigma.headless.serviceVariants.moveToNAS

import lorry.folder.items.dossiersigma.basics.domain.SigmaPath

data class ManifestEntry(
    val fullPath: SigmaPath,
    val picture64: String? // ta chaîne Base64 (nullable)
)