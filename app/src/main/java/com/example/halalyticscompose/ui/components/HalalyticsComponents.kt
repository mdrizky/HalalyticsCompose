package com.example.halalyticscompose.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.ripple
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.halalyticscompose.ui.theme.*

// ════════════════════════════════════════════════════════════════════
// SHIMMER EFFECT
// ════════════════════════════════════════════════════════════════════

@Composable
fun ShimmerBrush(
    showShimmer: Boolean = true,
    targetValue: Float = 1000f
): androidx.compose.ui.graphics.Brush {
    return if (showShimmer) {
        val shimmerColors = listOf(
            Color.LightGray.copy(alpha = 0.6f),
            Color.LightGray.copy(alpha = 0.2f),
            Color.LightGray.copy(alpha = 0.6f),
        )

        val transition = rememberInfiniteTransition(label = "")
        val translateAnimation = transition.animateFloat(
            initialValue = 0f,
            targetValue = targetValue,
            animationSpec = infiniteRepeatable(
                animation = tween(800), repeatMode = RepeatMode.Reverse
            ), label = ""
        )
        androidx.compose.ui.graphics.Brush.linearGradient(
            colors = shimmerColors,
            start = androidx.compose.ui.geometry.Offset.Zero,
            end = androidx.compose.ui.geometry.Offset(x = translateAnimation.value, y = translateAnimation.value)
        )
    } else {
        androidx.compose.ui.graphics.Brush.linearGradient(
            colors = listOf(Color.Transparent, Color.Transparent),
            start = androidx.compose.ui.geometry.Offset.Zero,
            end = androidx.compose.ui.geometry.Offset.Zero
        )
    }
}

@Composable
fun ShimmerCard(
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 120.dp,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(16.dp)
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .background(ShimmerBrush())
    )
}

// ════════════════════════════════════════════════════════════════════
// PRIMARY BUTTON
// ════════════════════════════════════════════════════════════════════

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    fullWidth: Boolean = false
) {
    Button(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        isLoading = isLoading,
        fullWidth = fullWidth,
        backgroundColor = Emerald,
        textColor = Color.White,
        borderColor = Color.Transparent
    )
}

// ════════════════════════════════════════════════════════════════════
// SECONDARY BUTTON
// ════════════════════════════════════════════════════════════════════

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    fullWidth: Boolean = false
) {
    Button(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        isLoading = isLoading,
        fullWidth = fullWidth,
        backgroundColor = Color.White,
        textColor = Teal,
        borderColor = Teal
    )
}

// ════════════════════════════════════════════════════════════════════
// GHOST BUTTON
// ════════════════════════════════════════════════════════════════════

@Composable
fun GhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    textColor: Color = Teal
) {
    Button(
        text = text,
        onClick = onClick,
        modifier = modifier.height(44.dp),
        enabled = enabled,
        backgroundColor = Color.Transparent,
        textColor = textColor,
        borderColor = Color.Transparent
    )
}

// ════════════════════════════════════════════════════════════════════
// BASE BUTTON COMPONENT
// ════════════════════════════════════════════════════════════════════

@Composable
private fun Button(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    fullWidth: Boolean = false,
    backgroundColor: Color,
    textColor: Color,
    borderColor: Color
) {
    val interactionSource = remember { MutableInteractionSource() }
    val animatedBgColor by animateColorAsState(
        targetValue = when {
            !enabled -> Slate400
            else -> backgroundColor
        }
    )
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "button_scale"
    )
    
    Row(
        modifier = modifier
            .scale(scale)
            .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier.wrapContentWidth())
            .height(HalalyticsDimensions.buttonHeight)
            .shadow(
                elevation = if (enabled && borderColor == Color.Transparent) HalalyticsShadows.elevation2 else 0.dp,
                shape = RoundedCornerShape(HalalyticsDimensions.radiusLarge)
            )
            .clip(RoundedCornerShape(HalalyticsDimensions.radiusLarge))
            .background(animatedBgColor)
            .border(
                width = 1.dp,
                color = if (borderColor != Color.Transparent) borderColor else animatedBgColor,
                shape = RoundedCornerShape(HalalyticsDimensions.radiusLarge)
            )
            .clickable(
                enabled = enabled && !isLoading,
                interactionSource = interactionSource,
                indication = ripple()
            ) { onClick() },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = textColor,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            color = if (enabled) textColor else Slate600,
            fontSize = 16.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

// ════════════════════════════════════════════════════════════════════
// TEXT INPUT FIELD
// ════════════════════════════════════════════════════════════════════

@Composable
fun TextInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    error: String? = null,
    isPassword: Boolean = false,
    helper: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Done
) {
    var isPasswordVisible by remember { mutableStateOf(false) }
    
    Column(modifier = modifier.fillMaxWidth()) {
        // Label
        if (label.isNotEmpty()) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Slate600,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
        
        // Input Field
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(HalalyticsDimensions.buttonHeight)
                .clip(RoundedCornerShape(HalalyticsDimensions.radiusLarge))
                .background(Color.White)
                .border(
                    width = 1.dp,
                    color = if (error != null) Error else Slate200,
                    shape = RoundedCornerShape(HalalyticsDimensions.radiusLarge)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    textStyle = androidx.compose.material3.LocalTextStyle.current.copy(
                        fontSize = 16.sp,
                        color = Slate900
                    ),
                    visualTransformation = if (isPassword && !isPasswordVisible) {
                        PasswordVisualTransformation()
                    } else {
                        VisualTransformation.None
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = keyboardType,
                        imeAction = imeAction
                    ),
                    decorationBox = { innerTextField ->
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                fontSize = 16.sp,
                                color = Slate400
                            )
                        }
                        innerTextField()
                    }
                )
                
                // Password toggle icon
                if (isPassword) {
                    IconButton(
                        onClick = { isPasswordVisible = !isPasswordVisible },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = "Toggle password visibility",
                            tint = Slate600
                        )
                    }
                }
            }
        }
        
        // Error or Helper text
        if (error != null) {
            Text(
                text = error,
                fontSize = 12.sp,
                color = Error,
                modifier = Modifier.padding(top = 6.dp)
            )
        } else if (helper != null) {
            Text(
                text = helper,
                fontSize = 12.sp,
                color = Slate500,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

// ════════════════════════════════════════════════════════════════════
// SEARCH INPUT
// ════════════════════════════════════════════════════════════════════

@Composable
fun SearchInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search..."
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(HalalyticsDimensions.buttonHeight)
            .clip(RoundedCornerShape(HalalyticsDimensions.radiusLarge))
            .background(Color.White)
            .border(
                width = 1.dp,
                color = Slate200,
                shape = RoundedCornerShape(HalalyticsDimensions.radiusLarge)
            )
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = "Search",
            tint = Slate500,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            textStyle = androidx.compose.material3.LocalTextStyle.current.copy(
                fontSize = 16.sp,
                color = Slate900
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = { onSearch(value) }
            ),
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        fontSize = 16.sp,
                        color = Slate400
                    )
                }
                innerTextField()
            }
        )
        
        if (value.isNotEmpty()) {
            IconButton(
                onClick = { onValueChange("") },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Clear,
                    contentDescription = "Clear search",
                    tint = Slate500
                )
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════
// CARD WITH GLASSMORPHISM
// ════════════════════════════════════════════════════════════════════

@Composable
fun Card(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    backgroundColor: Color = Color.White,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = if (onClick != null) interactionSource.collectIsPressedAsState().value else false
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "card_scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = if (isPressed) HalalyticsShadows.elevation1 else HalalyticsShadows.elevation2,
                shape = RoundedCornerShape(HalalyticsDimensions.radius2XLarge)
            )
            .clip(RoundedCornerShape(HalalyticsDimensions.radius2XLarge))
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = Slate200,
                shape = RoundedCornerShape(HalalyticsDimensions.radius2XLarge)
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        indication = ripple(),
                        interactionSource = interactionSource
                    ) { onClick() }
                } else {
                    Modifier
                }
            )
            .padding(HalalyticsDimensions.paddingMedium)
    ) {
        content()
    }
}

// ════════════════════════════════════════════════════════════════════
// GLASS CARD (OVERLAY/FLOATING)
// ════════════════════════════════════════════════════════════════════

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(HalalyticsDimensions.radius2XLarge))
            .background(GlassClear)
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.5f),
                shape = RoundedCornerShape(HalalyticsDimensions.radius2XLarge)
            )
            .shadow(
                elevation = HalalyticsShadows.elevation2,
                shape = RoundedCornerShape(HalalyticsDimensions.radius2XLarge)
            )
            .padding(HalalyticsDimensions.paddingMedium)
    ) {
        content()
    }
}

// ════════════════════════════════════════════════════════════════════
// EMPTY STATE
// ════════════════════════════════════════════════════════════════════

@Composable
fun EmptyState(
    icon: Painter,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    action: @Composable () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(HalalyticsDimensions.paddingLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = Slate400,
            modifier = Modifier.size(HalalyticsDimensions.icon2XLarge)
        )
        
        Spacer(modifier = Modifier.height(HalalyticsDimensions.space_6))
        
        Text(
            text = title,
            fontSize = HalalyticsTypography.headlineMedium,
            color = Slate900,
            modifier = Modifier.padding(horizontal = HalalyticsDimensions.paddingLarge)
        )
        
        Spacer(modifier = Modifier.height(HalalyticsDimensions.space_3))
        
        Text(
            text = message,
            fontSize = HalalyticsTypography.bodyMedium,
            color = Slate600,
            modifier = Modifier.padding(horizontal = HalalyticsDimensions.paddingLarge)
        )
        
        Spacer(modifier = Modifier.height(HalalyticsDimensions.space_6))
        
        action()
    }
}

// ════════════════════════════════════════════════════════════════════
// LOADING SPINNER
// ════════════════════════════════════════════════════════════════════

@Composable
fun LoadingSpinner(
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 48.dp,
    strokeWidth: androidx.compose.ui.unit.Dp = 4.dp
) {
    CircularProgressIndicator(
        modifier = modifier.size(size),
        color = Emerald,
        strokeWidth = strokeWidth
    )
}

// ════════════════════════════════════════════════════════════════════
// SKELETON LOADER
// ════════════════════════════════════════════════════════════════════

@Composable
fun SkeletonLoader(
    modifier: Modifier = Modifier,
    type: SkeletonType = SkeletonType.CARD
) {
    val shimmerAnimation = rememberInfiniteTransition(label = "shimmer")
    val shimmerProgress by shimmerAnimation.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500)
        ),
        label = "shimmer"
    )
    
    val baseColor = Slate200
    val highlightColor = Slate100
    
    val colors = listOf(
        baseColor,
        highlightColor,
        baseColor
    )
    
    when (type) {
        SkeletonType.CARD -> {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(HalalyticsDimensions.radius2XLarge))
                    .background(baseColor)
            )
        }
        SkeletonType.TEXT_LINE -> {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(baseColor)
            )
        }
        SkeletonType.CIRCLE -> {
            Box(
                modifier = modifier
                    .size(64.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(baseColor)
            )
        }
        SkeletonType.RECTANGLE -> {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(baseColor)
            )
        }
    }
}

enum class SkeletonType {
    CARD,
    TEXT_LINE,
    CIRCLE,
    RECTANGLE
}
