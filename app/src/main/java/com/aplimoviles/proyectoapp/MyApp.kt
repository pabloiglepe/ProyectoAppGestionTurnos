package com.aplimoviles.proyectoapp

import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.database.FirebaseDatabase


class MyApp: Application() {
    val db = Firebase.firestore

    override fun onCreate() {
        super.onCreate()

        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }
}


