package com.example.materialfilejetpackcompose

import android.content.res.Resources.Theme
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.tv.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ColorScheme
import androidx.tv.material3.DrawerValue
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.MaterialTheme.colorScheme
import androidx.tv.material3.ModalNavigationDrawer
import androidx.tv.material3.Surface
import androidx.tv.material3.rememberDrawerState
import com.example.materialfilejetpackcompose.ui.theme.MaterialFileJetpackComposeTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalTvMaterial3Api::class, ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialFileJetpackComposeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    HomePage()

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
}

