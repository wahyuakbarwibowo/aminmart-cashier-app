package com.wahyuakbarwibowo.aminmartkasir

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.wahyuakbarwibowo.aminmartkasir.data.local.AppDatabase
import com.wahyuakbarwibowo.aminmartkasir.data.remote.AuthManager
import com.wahyuakbarwibowo.aminmartkasir.ui.LoginGate
import com.wahyuakbarwibowo.aminmartkasir.ui.MainAppContainer
import com.wahyuakbarwibowo.aminmartkasir.ui.theme.AminmartKasirTheme

class MainActivity : ComponentActivity() {

    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize database
        database = AppDatabase.getDatabase(applicationContext)
        AuthManager.init(applicationContext)

        enableEdgeToEdge()
        setContent {
            AminmartKasirTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LoginGate(
                        onSignedIn = {
                            val viewModelFactory =
                                AppDatabase.getViewModelFactory(applicationContext)
                            MainAppContainer(viewModelFactory = viewModelFactory)
                        }
                    )
                }
            }
        }
    }
}
