package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Modifier extension that makes any Composable element focusable on Android TV / D-Pad
 * with scale animation, high-contrast glow border, and TV remote key event support (DPAD_CENTER / ENTER).
 */
@Composable
fun Modifier.tvFocusable(
    shape: Shape = RoundedCornerShape(12.dp),
    focusedBorderColor: Color = MaterialTheme.colorScheme.primary,
    focusedBorderWidth: Dp = 2.5.dp,
    focusedScale: Float = 1.03f,
    onClick: (() -> Unit)? = null
): Modifier {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isFocused) focusedScale else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "tv_focus_scale"
    )

    return this
        .scale(scale)
        .onFocusChanged { isFocused = it.isFocused }
        .focusable()
        .then(
            if (isFocused) {
                Modifier
                    .shadow(elevation = 8.dp, shape = shape, spotColor = focusedBorderColor)
                    .border(BorderStroke(focusedBorderWidth, focusedBorderColor), shape = shape)
            } else {
                Modifier
            }
        )
        .then(
            if (onClick != null) {
                Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onClick() }
                    .onKeyEvent { keyEvent ->
                        if (keyEvent.type == KeyEventType.KeyUp) {
                            when (keyEvent.key) {
                                Key.DirectionCenter, Key.Enter, Key.NumPadEnter -> {
                                    onClick()
                                    true
                                }
                                else -> false
                            }
                        } else false
                    }
            } else Modifier
        )
}
