package com.bda.projectpulse.ui.components

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SwipeRefresh(
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var refreshing by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    val threshold = with(LocalDensity.current) { 48.dp.toPx() }
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        if (progress >= threshold && !refreshing) {
                            refreshing = true
                            scope.launch {
                                onRefresh()
                                animate(
                                    initialValue = progress,
                                    targetValue = 0f
                                ) { value, _ -> progress = value }
                                refreshing = false
                            }
                        } else {
                            scope.launch {
                                animate(
                                    initialValue = progress,
                                    targetValue = 0f
                                ) { value, _ -> progress = value }
                            }
                        }
                    },
                    onDragCancel = {
                        scope.launch {
                            animate(
                                initialValue = progress,
                                targetValue = 0f
                            ) { value, _ -> progress = value }
                        }
                    },
                    onVerticalDrag = { change, dragAmount ->
                        if (dragAmount > 0 && !refreshing) {
                            progress = (progress + dragAmount).coerceIn(0f, threshold * 2)
                            change.consume()
                        }
                    }
                )
            }
    ) {
        content()
        
        if (progress > 0f || refreshing) {
            val alpha by animateFloatAsState(
                targetValue = if (refreshing) 1f else (progress / threshold).coerceIn(0f, 1f),
                label = "refresh_alpha"
            )
            
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset { IntOffset(0, (progress * 0.5f).roundToInt()) }
                    .size(36.dp)
                    .alpha(alpha),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp
            )
        }
    }
} 