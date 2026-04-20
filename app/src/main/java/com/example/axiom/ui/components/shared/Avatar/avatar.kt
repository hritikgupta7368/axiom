package com.example.axiom.ui.components.shared.Avatar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.axiom.ui.theme.AxiomTheme

enum class ProductImageShape {
    Circle,
    RoundedSquare
}

@Composable
fun AvatarBox(
    imageUrl: String? = null,
    selected: Boolean,
    shape: ProductImageShape = ProductImageShape.RoundedSquare
) {


    val avatarShape =
        if (shape == ProductImageShape.Circle) CircleShape
        else RoundedCornerShape(12.dp)

    Box(
        modifier = Modifier
            .padding(end = 16.dp)
            .size(48.dp)
    ) {

        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(avatarShape)
                .background(AxiomTheme.components.avatar.background)
                .border(
                    width = 1.dp,
                    color = AxiomTheme.components.avatar.border,
                    shape = avatarShape
                ),
            contentAlignment = Alignment.Center
        ) {

            if (imageUrl != null) {
//                AsyncImage(
//                    model = imageUrl,
//                    contentDescription = null,
//                    modifier = Modifier.fillMaxSize(),
//                    contentScale = ContentScale.Crop
//                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.ShoppingCart,
                    contentDescription = null,
                    tint = AxiomTheme.components.avatar.iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        if (selected) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 8.dp, y = (-8).dp)
                    .shadow(
                        elevation = 1.dp,
                        shape = CircleShape,
                        ambientColor = Color.Black.copy(0.05f),
                        spotColor = Color.Black.copy(0.05f)
                    )
                    .clip(CircleShape)
                    .background(Color(0xFF3B82F6))
                    .border(2.dp, AxiomTheme.components.avatar.tickBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }


    }
}