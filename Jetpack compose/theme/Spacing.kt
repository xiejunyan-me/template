package com.amazing.listener.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


/**
 *@className Spacing
 *@Author xiejunyan
 *@Date 2022/1/10
 *@Description
 */
data class Spacing(
    val default: Dp = 0.dp,
    val extraSmall: Dp = 4.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 16.dp,
    val large: Dp = 32.dp,
    val extraLarge: Dp = 64.dp,
)

val LocalSpacing = compositionLocalOf { Spacing(default = 5.dp) }

val MaterialTheme.Spacing: Spacing
    @Composable
    @ReadOnlyComposable
    get() = LocalSpacing.current

//CompositionLocalProvider(LocalSpacing provides Spacing()) {
//    MaterialTheme(
//        colors = colors,
//        typography = Typography,
//        shapes = Shapes,
//        content = content
//    )
//}

//站在巨人肩膀：https://www.youtube.com/watch?v=S61mXlMQkLY&ab_channel=PhilippLackner