package com.bda.projectpulse

import android.app.Application
import com.google.firebase.FirebaseApp

class ProjectPulseApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
} 