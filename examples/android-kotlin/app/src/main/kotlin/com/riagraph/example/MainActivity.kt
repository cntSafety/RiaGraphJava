package com.riagraph.example

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import com.riagraph.Database
import com.riagraph.SystemConfig

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val config = exampleConfig()
        setContentView(TextView(this).apply {
            text = "RiaGraph Kotlin example: ${config.maxNumThreads} threads"
        })
    }
}

fun exampleConfig(): SystemConfig = SystemConfig().apply {
    maxNumThreads = 2
    enableDefaultHashIndex = true
}

fun openDatabase(path: String, config: SystemConfig): Database = Database(path, config)
