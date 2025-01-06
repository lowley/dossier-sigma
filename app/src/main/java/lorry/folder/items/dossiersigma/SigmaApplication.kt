package lorry.folder.items.dossiersigma

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SigmaApplication : Application() {
    
    companion object{
        val APPLICATION_NAME = "SigmaApplication"
        
    }
}