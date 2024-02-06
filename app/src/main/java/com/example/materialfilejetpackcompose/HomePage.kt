package com.example.materialfilejetpackcompose

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandIn
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role.Companion.Switch
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage() {
    var isExpanded by remember { mutableStateOf(false) }
    val widthAnim by animateDpAsState(targetValue = if (isExpanded) 200.dp else 64.dp)

    Surface {
        Box {
            Column(
                Modifier
                    .fillMaxSize()
                    .animateContentSize(), // Add this line
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val context = LocalContext.current
                Button(onClick = {
                    Toast.makeText(context, "Button clicked", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Click me")
                }
            }
            Column(
                Modifier
                    .fillMaxHeight()
                    .width(widthAnim)
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(horizontal = 10.dp, vertical = 10.dp)
                    .onFocusChanged { focusState ->
                        isExpanded = focusState.isFocused
                    }
                    .animateContentSize(), // Add this line
                horizontalAlignment = Alignment.Start
            ) {
                Icon(
                    Icons.Default.Menu,
                    modifier = Modifier
                        .clickable(
                            onClick = {
                                isExpanded = !isExpanded
                            }
                        ),
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(modifier = Modifier.height(20.dp))
                DrawerItem(Icons.Default.Folder, "All Files", isExpanded) {}
                DrawerItem(Icons.Default.Photo, "Photos", isExpanded) {}
                DrawerItem(Icons.Default.VideoLibrary, "Videos", isExpanded) {}
                DrawerItem(Icons.Default.AudioFile, "Audios", isExpanded) {}
                Spacer(modifier = Modifier.height(80.dp))

            }
        }
    }
}

@Composable
fun DrawerItem(icon: ImageVector, title: String, expanded: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(24.dp)
        )
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandIn(),
            exit = fadeOut() + shrinkOut()
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = title, color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

