package com.example.materialfilejetpackcompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.materialfilejetpackcompose.ui.theme.MaterialFileJetpackComposeTheme

class MainActivity : ComponentActivity() {
    private val themeManager = ThemeManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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


