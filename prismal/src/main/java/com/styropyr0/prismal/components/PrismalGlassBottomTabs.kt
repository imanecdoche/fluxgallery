package com.styropyr0.prismal.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceIn
import androidx.compose.ui.util.fastRoundToInt
import kotlin.math.abs
import androidx.compose.ui.util.lerp
import com.styropyr0.prismal.PrismalBackdrop
import com.styropyr0.prismal.PrismalGlass
import com.styropyr0.prismal.PrismalGlassPipeline
import com.styropyr0.prismal.sources.prismalGlassLayer
import com.styropyr0.prismal.sources.rememberPrismalMergedSource
import com.styropyr0.prismal.sources.rememberPrismalGlassLayer
import com.styropyr0.prismal.sources.rememberPrismalWrappedSource
import com.styropyr0.prismal.drawPrismalGlass
import com.styropyr0.prismal.effects.applyPrismalGlassEffects
import com.styropyr0.prismal.effects.prismalBlur
import com.styropyr0.prismal.effects.prismalLens
import com.styropyr0.prismal.specular.PrismalSpecular
import com.styropyr0.prismal.interactive.PrismalSpringMotion
import com.styropyr0.prismal.interactive.PrismalPressRipple
import com.styropyr0.prismal.depth.PrismalDepthInset
import com.styropyr0.prismal.depth.PrismalDepthShadow
import com.styropyr0.prismal.shapes.PrismalCapsule
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sign

/**
 * iOS-style glass bottom tab bar with a sliding selection pill and press feedback.
 *
 * @param selectedTabIndex Index of the currently selected tab.
 * @param onTabSelected Called when the user selects a tab.
 * @param backdrop Source sampled through the tab bar glass.
 * @param tabsCount Number of tabs (must match [content] child count).
 * @param content Tab items, typically [PrismalGlassBottomTab] composables.
 * @param tintDropletContent When true, tints tab content sampled by the selection
 *   droplet. Set to false to keep the colors from [content].
 * @param dropletContentTint Color used when [tintDropletContent] is true. When `null`,
 *   defaults to iOS-style blue (`#0088FF` light / `#0091FF` dark). Pass any [Color]
 *   (e.g. [Color.Black]) to override.
 * @param specular The specular setting for the base pill
 * @param blurRadius Blur of the backdrop behind the bar.
 * @param refractionHeight Edge lens zone height of the bar.
 * @param refractionAmount Edge lens displacement of the bar.
 * @param dropletBlurRadius Blur of the bar as seen through the selection droplet. `null`
 *   (default) uses [blurRadius], so the droplet shows the bar exactly as it is.
 * @param dropletChromaticAberration Dispersion strength of the droplet lens (`0f` = off;
 *   values above `1f` exaggerate the color split).
 * @param dropletRefractionHeight Edge lens zone height of the droplet while pressed.
 * @param dropletRefractionAmount Edge lens displacement of the droplet while pressed. Larger
 *   values also make the chromatic aberration more visible.
 * @param dropletContentScaleY Vertical scale of the tab row seen through the droplet while
 *   pressed, in `[0, 1]`. Lower values show a flatter, more shrunken bar; `1f` disables it.
 * @param renderWithLowOverhead How the droplet grows / squashes while pressed or dragged.
 *   `true` (default) scales its graphics layer: cheap and perfectly smooth, but the
 *   rasterized lens is resampled, so the pressed droplet looks slightly soft. `false`
 *   re-lays the droplet out at its animated size and snaps it to whole pixels, so it renders
 *   1:1 and sharp, at the cost of a relayout per animation frame (can stutter on weaker
 *   devices).
 * @param pressGlow Intensity of the white press glow on the bar in `[0, 1]`.
 */
@Composable
fun PrismalGlassBottomTabs(
    selectedTabIndex: () -> Int,
    onTabSelected: (index: Int) -> Unit,
    backdrop: PrismalBackdrop,
    tabsCount: Int,
    modifier: Modifier = Modifier,
    adaptiveLuminance: Boolean = false,
    luminance: () -> Float = { 0.5f },
    tintDropletContent: Boolean = true,
    dropletContentTint: Color? = null,
    specular: (() -> PrismalSpecular?)? = { PrismalSpecular.Default },
    blurRadius: Dp = 2.dp,
    refractionHeight: Dp = 24.dp,
    refractionAmount: Dp = 24.dp,
    dropletBlurRadius: Dp? = null,
    dropletChromaticAberration: Float = 0.7f,
    dropletRefractionHeight: Dp = 20.dp,
    dropletRefractionAmount: Dp = 10.dp,
    pressGlow: Float = 1f,
    dropletContentScaleY: Float = 0.90f,
    renderWithLowOverhead: Boolean = true,
    showDroplet: Boolean = true,
    enableTabGestures: Boolean = true,
    containerColor: Color? = null,
    content: @Composable RowScope.() -> Unit
) {
    val isLightTheme = !isSystemInDarkTheme()
    val resolvedContainerColor = containerColor ?: if (isLightTheme) Color(0xFFFAFAFA).copy(0.45f)
    else Color(0xFF1C1C1E).copy(0.65f)

    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    val viewConfig = LocalViewConfiguration.current
    val touchSlop = viewConfig.touchSlop

    BoxWithConstraints(
        modifier,
        contentAlignment = Alignment.CenterStart
    ) {
        val tabWidth = with(density) {
            (constraints.maxWidth.toFloat() - 8.dp.toPx()) / tabsCount
        }

        val offsetAnimation = remember { Animatable(0f) }
        val panelOffset by remember(density) {
            derivedStateOf {
                val fraction = (offsetAnimation.value / constraints.maxWidth).fastCoerceIn(-1f, 1f)
                with(density) {
                    4.dp.toPx() * fraction.sign * EaseOut.transform(abs(fraction))
                }
            }
        }

        val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
        val animationScope = rememberCoroutineScope()
        var dragHighlightIndex by remember { mutableStateOf<Int?>(null) }
        val highlightedTabIndex = dragHighlightIndex ?: selectedTabIndex()
        val dampedDragAnimation = remember(animationScope, tabsCount, tabWidth, isLtr) {
            PrismalSpringMotion(
                animationScope = animationScope,
                initialValue = selectedTabIndex().toFloat(),
                valueRange = 0f..(tabsCount - 1).toFloat(),
                visibilityThreshold = 0.001f,
                initialScale = 1f,
                pressedScale = DropletPressedScale,
                onDragStarted = {
                    dragHighlightIndex = value.fastRoundToInt().coerceIn(0, tabsCount - 1)
                },
                onDragStopped = {
                    val targetIndex = targetValue.fastRoundToInt().coerceIn(0, tabsCount - 1)
                    dragHighlightIndex = targetIndex
                    animateToValue(targetIndex.toFloat())
                    onTabSelected(targetIndex)
                    animationScope.launch {
                        offsetAnimation.animateTo(
                            0f,
                            spring(1f, 300f, 0.5f)
                        )
                    }
                },
                onDrag = { _, dragAmount ->
                    val nextValue =
                        (targetValue + dragAmount.x / tabWidth * if (isLtr) 1f else -1f).fastCoerceIn(
                            0f,
                            (tabsCount - 1).toFloat()
                        )
                    updateValue(nextValue)
                    dragHighlightIndex = nextValue.fastRoundToInt()
                    animationScope.launch {
                        offsetAnimation.snapTo(offsetAnimation.value + dragAmount.x)
                    }
                }
            )
        }

        val targetIndex = selectedTabIndex()
        LaunchedEffect(targetIndex) {
            dragHighlightIndex = null
            dampedDragAnimation.animateToValue(targetIndex.toFloat())
        }

        val interactivePrismalSpecular = remember(animationScope, tabWidth, isLtr, pressGlow) {
            PrismalPressRipple(
                animationScope = animationScope,
                intensity = pressGlow,
                position = { size, _ ->
                    Offset(
                        if (isLtr) (dampedDragAnimation.value + 0.5f) * tabWidth + panelOffset
                        else size.width - (dampedDragAnimation.value + 0.5f) * tabWidth + panelOffset,
                        size.height / 2f
                    )
                }
            )
        }

        // 1. Outer Pill Container (Glass backdrop blur & refraction over photos)
        Box(
            Modifier
                .graphicsLayer {
                    translationX = panelOffset
                }
                .drawPrismalGlass(
                    backdrop = backdrop,
                    shape = { PrismalCapsule() },
                    specular = specular,
                    effects = {
                        applyPrismalGlassEffects(
                            density = density,
                            adaptiveLuminance = adaptiveLuminance,
                            luminance = luminance(),
                            blurRadiusPx = with(density) { blurRadius.toPx() },
                            refractionHeightPx = with(density) { refractionHeight.toPx() },
                            refractionAmountPx = with(density) { refractionAmount.toPx() }
                        )
                    },
                    layerBlock = {
                        val progress = dampedDragAnimation.pressProgress
                        val scale = lerp(1f, 1f + 16.dp.toPx() / size.width, progress)
                        scaleX = scale
                        scaleY = scale
                    },
                    onDrawSurface = { drawRect(resolvedContainerColor) }
                )
                .then(interactivePrismalSpecular.modifier)
                .height(64.dp)
                .fillMaxWidth()
        )

        val dropletAlpha by animateFloatAsState(
            targetValue = if (showDroplet) 1f else 0f,
            animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f),
            label = "DropletAlpha"
        )
        val dropletScaleTarget by animateFloatAsState(
            targetValue = if (showDroplet) 1f else 0f,
            animationSpec = spring(dampingRatio = 0.68f, stiffness = 420f),
            label = "DropletScale"
        )

        // 2. Sliding Liquid Glass Droplet Indicator (Pure refraction over backdrop, zero duplicate tabs)
        Box(
            Modifier
                .padding(horizontal = 4.dp, vertical = 4.dp)
                .graphicsLayer {
                    val x = if (isLtr) dampedDragAnimation.value * tabWidth + panelOffset
                            else size.width - (dampedDragAnimation.value + 1f) * tabWidth + panelOffset
                    translationX = if (renderWithLowOverhead) x else x.fastRoundToInt().toFloat()
                    alpha = dropletAlpha
                    scaleX = dropletScaleTarget
                    scaleY = dropletScaleTarget
                }
                .height(56.dp)
                .fillMaxWidth(1f / tabsCount)
                .drawPrismalGlass(
                    backdrop = backdrop,
                    shape = { PrismalCapsule() },
                    effects = {
                        val effectScale = if (renderWithLowOverhead) 1f else dampedDragAnimation.scaleY
                        val blurPx = dropletBlurRadius?.let { with(density) { it.toPx() } } ?: 0f
                        if (blurPx > 0f) {
                            prismalBlur(blurPx)
                        }
                        prismalLens(
                            refractionHeight = with(density) { dropletRefractionHeight.toPx() } * effectScale,
                            refractionAmount = with(density) { dropletRefractionAmount.toPx() } * effectScale,
                            depthEffect = true,
                            chromaticAberration = dropletChromaticAberration.coerceAtLeast(0f)
                        )
                    },
                    specular = {
                        PrismalSpecular.Default.copy(alpha = 0.85f)
                    },
                    depthShadow = {
                        PrismalDepthShadow(alpha = 0.30f)
                    },
                    depthInset = {
                        PrismalDepthInset(
                            radius = 6.dp,
                            alpha = 0.35f
                        )
                    },
                    layerBlock = if (renderWithLowOverhead) {
                        {
                            val (sx, sy) = dropletScale(dampedDragAnimation)
                            scaleX = sx
                            scaleY = sy
                        }
                    } else null,
                    onDrawSurface = {
                        drawRect(
                            if (isLightTheme) Color.White.copy(0.48f)
                            else Color.White.copy(0.36f)
                        )
                    }
                )
        )

        // 3. Tab Items Row (Single clean layer rendered on top: zero ghosting, crisp icons & text)
        CompositionLocalProvider(
            LocalPrismalBottomTabScale provides {
                lerp(1f, 1.05f, dampedDragAnimation.pressProgress)
            },
            LocalPrismalBottomTabHighlightedIndex provides { highlightedTabIndex }
        ) {
            Row(
                Modifier
                    .graphicsLayer {
                        translationX = panelOffset
                    }
                    .height(64.dp)
                    .fillMaxWidth()
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                content = content
            )
        }

        // 4. Interactive Touch & Drag Surface (Covers entire navbar: fluid 1:1 drag tracking + magnetic snap)
        if (enableTabGestures) {
            var currentDragValue by remember { mutableFloatStateOf(0f) }
            var lastHighlighted by remember { mutableIntStateOf(-1) }

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .pointerInput(tabWidth, tabsCount, isLtr) {
                        val padStartPx = with(density) { 4.dp.toPx() }
                        detectTapGestures { offset ->
                            val relativeX = offset.x - padStartPx
                            val rawIndex = (relativeX / tabWidth).toInt()
                            val tappedIndex = (if (isLtr) rawIndex else (tabsCount - 1 - rawIndex)).coerceIn(0, tabsCount - 1)
                            dragHighlightIndex = null
                            dampedDragAnimation.animateToValue(tappedIndex.toFloat())
                            onTabSelected(tappedIndex)
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                    }
                    .pointerInput(tabWidth, tabsCount, isLtr) {
                        detectHorizontalDragGestures(
                            onDragStart = {
                                currentDragValue = dampedDragAnimation.value
                                dampedDragAnimation.press()
                            },
                            onDragEnd = {
                                val finalIndex = currentDragValue.fastRoundToInt().coerceIn(0, tabsCount - 1)
                                dragHighlightIndex = null
                                dampedDragAnimation.animateToValue(finalIndex.toFloat())
                                onTabSelected(finalIndex)
                                dampedDragAnimation.release()
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            },
                            onDragCancel = {
                                val finalIndex = currentDragValue.fastRoundToInt().coerceIn(0, tabsCount - 1)
                                dragHighlightIndex = null
                                dampedDragAnimation.animateToValue(finalIndex.toFloat())
                                dampedDragAnimation.release()
                            },
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                currentDragValue = (currentDragValue + dragAmount / tabWidth * (if (isLtr) 1f else -1f))
                                    .fastCoerceIn(0f, (tabsCount - 1).toFloat())
                                dampedDragAnimation.snapToValue(currentDragValue)
                                val currentHighlighted = currentDragValue.fastRoundToInt().coerceIn(0, tabsCount - 1)
                                if (currentHighlighted != lastHighlighted) {
                                    lastHighlighted = currentHighlighted
                                    dragHighlightIndex = currentHighlighted
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                            }
                        )
                    }
            )
        }
    }
}

private const val DropletPressedScale = 78f / 56f

private fun dropletScale(motion: PrismalSpringMotion): Pair<Float, Float> {
    val velocity = motion.velocity / 10f
    val sx = motion.scaleX / (1f - (velocity * 0.75f).fastCoerceIn(-0.2f, 0.2f))
    val sy = motion.scaleY * (1f - (velocity * 0.25f).fastCoerceIn(-0.2f, 0.2f))
    return sx to sy
}
