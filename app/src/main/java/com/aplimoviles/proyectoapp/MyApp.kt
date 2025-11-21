package com.aplimoviles.proyectoapp

import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class MyApp : Application() {

    lateinit var db: FirebaseFirestore
    lateinit var database: FirebaseDatabase

    private val URL =
        "https://proyectogestionturnos-default-rtdb.europe-west1.firebasedatabase.app"

    override fun onCreate() {
        super.onCreate()

        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
        }

        db = Firebase.firestore

        database = Firebase.database(URL)

        database.setPersistenceEnabled(true)
    }
}