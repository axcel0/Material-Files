package com.example.materialfilejetpackcompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.materialfilejetpackcompose.ui.theme.MaterialFileJetpackComposeTheme
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : ComponentActivity() {
    private var themeManager = ThemeManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this) {

                //add dialog to confirm exit
                MaterialAlertDialogBuilder(this@MainActivity)
                    .setTitle("Exit")
                    .setMessage("Are you sure you want to exit?")
                    .setPositiveButton("Yes") { _, _ ->
                        finish()
                    }
                    .setNegativeButton("No") { _, _ ->
                        // Respond to negative button press
                    }
                    .show()

        }
        setContent {
            MaterialFileJetpackComposeTheme(themeManager.darkTheme.value) {
                MyApp(themeManager)
            }
        }
    }
}


//    @OptIn(ExperimentalTvMaterial3Api::class, ExperimentalFoundationApi::class)
//    @Composable
//    fun SidebarContent() {
//        var presses by remember { mutableIntStateOf(0) }
//        Column(modifier = Modifier.fillMaxSize()) {
//            Text(
//                text = "Sidebar",
//                textAlign = TextAlign.Center,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(16.dp)
//            )
//            LazyColumn {
//                items(presses + 1) {
//                    Row(
//                        modifier = Modifier.combinedClickable(
//                            onClick = { presses++ },
//                            onLongClick = {
//                                presses = 0
//                            },
//
//                            role = Role.Button,
//                        )
//                    ) {
//                        Text("Item $it")
//                        IconButton(onClick = {
//                            presses++
//                        }) {
//                            Icon(Icons.Filled.Add, contentDescription = "Add")
//                        }
//                    }
//                }
//            }
//        }
//    }


