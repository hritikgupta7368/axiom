package com.example.axiom.ui.components.shared.cards

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.AccountBox
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.axiom.ui.components.shared.Avatar.AvatarBox
import com.example.axiom.ui.components.shared.Avatar.ProductImageShape
import com.example.axiom.ui.components.shared.Chip
import com.example.axiom.ui.components.shared.ChipColor
import com.example.axiom.ui.screens.finances.Invoice.components.InvoiceCardDto
import com.example.axiom.ui.screens.finances.Invoice.components.InvoiceStatus
import com.example.axiom.ui.screens.finances.Invoice.components.PaymentStatus
import com.example.axiom.ui.screens.finances.Invoice.components.SupplyType
import com.example.axiom.ui.screens.finances.customer.components.PartyEntity
import com.example.axiom.ui.screens.finances.product.components.ProductBasic
import com.example.axiom.ui.screens.finances.purchase.components.PurchaseRecordEntity
import com.example.axiom.ui.theme.AxiomTheme
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class MetricTimeFrame {
    MONTHLY, ANNUALLY
}

@Composable
fun MetricCard(
    modifier: Modifier = Modifier,
    title: String,
    valueMonthly: String,
    valueAnnually: String,
    barHeightsMonthly: List<Float>,
    barHeightsAnnually: List<Float>,
    greenBgColor: Color,
    greenTextColor: Color,
    chartBarColor: Color,
    chartActiveColor: Color
) {
    // State to track the current selected timeframe
    var timeFrame by remember { mutableStateOf(MetricTimeFrame.MONTHLY) }

    val isMonthly = timeFrame == MetricTimeFrame.MONTHLY

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = AxiomTheme.components.card.background),
        modifier = modifier
            .height(140.dp)
            .border(1.dp, AxiomTheme.components.card.border, RoundedCornerShape(24.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // --- HEADER ROW ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Animated Title (Soft Crossfade to match the breathe effect)
                AnimatedContent(
                    targetState = title,
                    transitionSpec = {
                        fadeIn(tween(300)) togetherWith fadeOut(tween(300))
                    },
                    label = "title_animation"
                ) { title ->
                    Text(
                        text = title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = AxiomTheme.components.card.subtitle
                    )
                }

                // Interactive Toggle Chip
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(greenBgColor)
                        .clickable {
                            timeFrame =
                                if (isMonthly) MetricTimeFrame.ANNUALLY else MetricTimeFrame.MONTHLY
                        }
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AnimatedContent(
                        targetState = isMonthly,
                        transitionSpec = {
                            (slideInVertically(tween(300)) { if (targetState) -it else it } + fadeIn(
                                tween(300)
                            )) togetherWith
                                    (slideOutVertically(tween(300)) { if (targetState) it else -it } + fadeOut(
                                        tween(300)
                                    ))
                        },
                        label = "chip_animation"
                    ) { state ->
                        Text(
                            text = if (state) "Monthly" else "Annually",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = greenTextColor
                        )
                    }
                }
            }

            // --- BOTTOM ROW (THE "BREATHE" ANIMATION) ---
            AnimatedContent(
                targetState = isMonthly,
                transitionSpec = {
                    // 1. Old content scales down to 95% and fades out over 200ms
                    // 2. New content delays slightly, then scales up from 95% and fades in over 300ms
                    (fadeIn(tween(300, delayMillis = 150)) + scaleIn(
                        tween(300, delayMillis = 150),
                        initialScale = 0.95f
                    )) togetherWith
                            (fadeOut(tween(200)) + scaleOut(tween(200), targetScale = 0.95f))
                },
                label = "breathe_animation"
            ) { stateIsMonthly ->

                val currentValue = if (stateIsMonthly) valueMonthly else valueAnnually
                val currentBars = if (stateIsMonthly) barHeightsMonthly else barHeightsAnnually

                // State to delay the bar heights from sprouting until the crossfade finishes
                var playBarAnimation by remember { mutableStateOf(false) }

                LaunchedEffect(stateIsMonthly) {
                    // Wait for the old content to completely fade out (200ms)
                    // + a tiny buffer so it happens AS the new content fades in
                    delay(200)
                    playBarAnimation = true
                }

                Column {
                    var textSizeMultiplier by remember { mutableFloatStateOf(1f) }

                    Text(
                        text = currentValue,
                        fontSize = 20.sp * textSizeMultiplier,
                        fontWeight = FontWeight.Bold,
                        color = AxiomTheme.components.card.title,
                        maxLines = 1,
                        softWrap = false,
                        onTextLayout = { textLayoutResult ->
                            if (textLayoutResult.hasVisualOverflow) {
                                textSizeMultiplier *= 0.9f
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Animated Bar Chart
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        currentBars.forEachIndexed { index, targetHeightFraction ->

                            // Start at 0f (invisible). Once playBarAnimation is true, animate to target.
                            val actualTarget = if (playBarAnimation) targetHeightFraction else 0f

                            val animatedHeight by animateFloatAsState(
                                targetValue = actualTarget,
                                animationSpec = tween(
                                    durationMillis = 500,
                                    delayMillis = index * 20, // Tiny stagger effect for premium feel
                                    easing = FastOutSlowInEasing
                                ),
                                label = "bar_height_$index"
                            )

                            val isLast = index == currentBars.lastIndex
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    // Use max(0.01f) so Compose doesn't crash on exactly 0 height in some edge cases
                                    .fillMaxHeight(animatedHeight.coerceAtLeast(0.01f))
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(chartActiveColor)
                            )
                            if (!isLast) Spacer(modifier = Modifier.width(4.dp))
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun HeaderCard(
    surfaceColor: Color,
    borderColor: Color,
    textColor: Color,
    textMutedColor: Color,
    badgeBgColor: Color,
    badgeTextColor: Color
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),//surface

        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(24.dp))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeBgColor)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "TAX HEALTH",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeTextColor,
                        letterSpacing = 1.sp
                    )
                }
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "Link",
                    tint = textMutedColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Estimated Net Tax",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Outward tax offset by eligible purchases",
                fontSize = 14.sp,
                color = textMutedColor
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatItem(
                    weight = 1f,
                    value = "24",
                    label = "Outward Tax",
                    textColor = textColor,
                    mutedColor = textMutedColor,
                    hasDivider = true,
                    dividerColor = badgeBgColor
                )
                StatItem(
                    weight = 1f,
                    value = "8.4k",
                    label = "Users",
                    textColor = textColor,
                    mutedColor = textMutedColor,
                    hasDivider = true,
                    dividerColor = badgeBgColor
                )
                StatItem(
                    weight = 1f,
                    value = "99%",
                    label = "Uptime",
                    textColor = textColor,
                    mutedColor = textMutedColor,
                    hasDivider = false,
                    dividerColor = badgeBgColor
                )
            }
        }
    }
}


@Composable
fun RowScope.StatItem(
    weight: Float,
    value: String,
    label: String,
    textColor: Color,
    mutedColor: Color,
    hasDivider: Boolean,
    dividerColor: Color
) {
    Box(
        modifier = Modifier.weight(weight),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = textColor)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = mutedColor)
        }
        if (hasDivider) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(1.dp)
                    .height(32.dp)
                    .background(dividerColor)
            )
        }
    }
}


@Composable
fun ActivityItem(
    title: String,
    subtitle: String,
    time: String,
    icon: ImageVector,
    surfaceColor: Color,
    borderColor: Color,
    textColor: Color,
    textMutedColor: Color,
    iconBgColor: Color
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textMutedColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = textMutedColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = time,
                fontSize = 11.sp,
                color = textMutedColor
            )
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProductCard(
    product: ProductBasic,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,

    ) {

    val borderColor =
        if (isSelected) AxiomTheme.components.card.selectedBorder else AxiomTheme.components.card.border
    val background =
        if (isSelected) AxiomTheme.components.card.selectedBackground else AxiomTheme.components.card.background

    val borderWidth = if (isSelected) 2.dp else 1.dp

    // --- Card Container ---
    Box(
        modifier = Modifier
            .padding(bottom = 12.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)) // rounded-[20px]
            .background(background)
            .border(borderWidth, borderColor, RoundedCornerShape(20.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(16.dp) // p-4
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            AvatarBox(
                selected = isSelected,
                shape = ProductImageShape.RoundedSquare
            )

            // --- Middle: Product Details ---
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    fontSize = 14.sp, // text-[14px]
                    fontWeight = FontWeight.SemiBold,
                    color = AxiomTheme.components.card.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(bottom = 1.dp) // mb-0.5
                )

                Text(
                    text = "HSN: ${product.hsn}",
                    fontSize = 11.sp, // text-[12px]
                    color = AxiomTheme.components.card.subtitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // --- Right: Pricing ---
            Column(
                modifier = Modifier.padding(start = 16.dp), // ml-4
                horizontalAlignment = Alignment.End
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "₹ ${product.sellingPrice}",
                        fontSize = 14.sp, // text-[14px]
                        fontWeight = FontWeight.Bold,
                        color = AxiomTheme.components.card.title,
                        modifier = Modifier.padding(bottom = 2.dp) // mb-0.5
                    )
                    Text(
                        text = " / ${product.unit}",
                        fontSize = 11.sp, // text-[11px]
                        fontWeight = FontWeight.Normal,
                        color = AxiomTheme.components.card.subtitle,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

            }
        }
    }
}


// --- List Item Component ---
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CustomerCard(
    customer: PartyEntity,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    balanceType: String = "Dr",
) {
    // Tailwind specific colors from your HTML
    val primaryColor = Color(0xFF3B82F6) // Tailwind blue-500

    val borderColor =
        if (isSelected) AxiomTheme.components.card.selectedBorder else AxiomTheme.components.card.border

    val cardShape = RoundedCornerShape(20.dp)

    val hasGst = !customer.gstNumber.isNullOrBlank()

    Box(
        modifier = Modifier
            .padding(bottom = 12.dp)
            .fillMaxWidth()
            .clip(cardShape)
            .background(AxiomTheme.components.card.background)
            .border(1.dp, borderColor, cardShape)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            AvatarBox(selected = isSelected, shape = ProductImageShape.Circle)

            // --- Center Details Section ---
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 2.dp)
                ) {
                    // Business Name
                    Text(
                        text = customer.businessName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AxiomTheme.components.card.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false) // Allows tag to stay visible
                    )

                    Spacer(modifier = Modifier.width(8.dp))


                    if (hasGst) Chip("GST REG", ChipColor.GREEN) else Chip("UNREG", ChipColor.GRAY)

                }
                // Subtitle / GST Number
                Text(
                    text = if (hasGst) "GST: ${customer.gstNumber}".uppercase() else "No GSTIN",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (hasGst) AxiomTheme.components.card.subtitle else AxiomTheme.components.card.mutedText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontStyle = if (hasGst) FontStyle.Normal else FontStyle.Italic
                )
            }

            // --- Right Amounts Section ---
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(start = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.padding(bottom = 2.dp)
                ) {
                    // Dynamically set amount color based on Dr/Cr type
                    val amountColor = when (balanceType.lowercase()) {
                        "dr" -> Color(0xFFF87171) // red-600
                        "cr" -> Color(0xFF16A34A) // green-600
                        else -> Color.White
                    }

                    Text(
                        text = "₹ ${customer.openingBalance}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = amountColor
                    )

                    if (balanceType.isNotEmpty()) {
                        Text(
                            text = balanceType,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = amountColor.copy(alpha = 0.7f),
                            modifier = Modifier.padding(start = 4.dp, bottom = 1.dp)
                        )
                    }
                }

                Text(
                    text = "Limit: ₹ ${customer.creditLimit}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = AxiomTheme.components.card.mutedText // gray-400
                )
            }
        }
    }
}


@Composable
fun InvoiceCard(
    invoice: InvoiceCardDto, // Assuming this contains status, paymentStatus, type, isEdited, etc.
    onLongClick: () -> Unit,
    onClick: () -> Unit
) {

    // Interaction state for click bounce effect
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        label = "card_scale"
    )


    val isCancelled = invoice.status == InvoiceStatus.CANCELLED


    val formatter = remember {
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    }

    val dateStr = formatter.format(Date(invoice.invoiceDate))



    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .padding(bottom = 12.dp)
            .shadow(
                elevation = 1.dp,
                ambientColor = Color.Black.copy(alpha = 0.05f),
                spotColor = Color.Black.copy(alpha = 0.05f),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null, // Removes default ripple, we are using scale bounce
                onClick = {}
            )
            .alpha(if (isCancelled) 0.75f else 1f)
    ) {
        // --- Main Card Content ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
//                .clickable { onClick() }
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                )
                .background(AxiomTheme.components.card.background)
                .border(1.dp, AxiomTheme.components.card.border, RoundedCornerShape(20.dp))
                .padding(16.dp), // p-4
        ) {

            // Row 1: Customer Name & Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = invoice.customerName,
                    fontSize = 17.sp, // text-[15px]
                    fontWeight = FontWeight.SemiBold,
                    color = AxiomTheme.components.card.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (isCancelled) TextDecoration.LineThrough else TextDecoration.None,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 16.dp) // pr-4
                )
                Text(
                    text = "₹ ${String.format(Locale.US, "%,.0f", invoice.grandTotal)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCancelled) AxiomTheme.components.card.mutedText else Color(
                        0xFF0D9488
                    ),
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.height(5.dp))
            // Row 2: Invoice No & Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp) // gap-1.5
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AccountBox, // Material equivalent to receipt_long
                        contentDescription = "Invoice",
                        tint = AxiomTheme.components.card.mutedText,
                        modifier = Modifier.size(12.dp) // text-[14px]
                    )
                    Text(
                        text = "INV-${invoice.invoiceNumber}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = AxiomTheme.components.card.subtitle,
                        textDecoration = if (isCancelled) TextDecoration.LineThrough else TextDecoration.None
                    )
                }

                Text(
                    text = dateStr,
                    fontSize = 12.sp,
                    color = AxiomTheme.components.card.subtitle
                )
            }

            Spacer(modifier = Modifier.height(11.dp))

            // Row 3: Tags & Edited Status
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp), // pt-1
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Wrap tags inside a Row (Use FlowRow if you expect many tags and want wrapping)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp), // gap-2
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isCancelled) Chip("Cancelled", ChipColor.RED)
                    if (invoice.status == InvoiceStatus.ACTIVE) Chip("ACTIVE", ChipColor.BLUE)
                    if (invoice.supplyType == SupplyType.INTER_STATE) Chip(
                        "Inter-State",
                        ChipColor.GRAY
                    ) else Chip("Intra-State", ChipColor.GRAY)
                    if (invoice.paymentStatus == PaymentStatus.PAID) Chip(
                        "Paid",
                        ChipColor.ORANGE
                    )
                    else if (invoice.paymentStatus == PaymentStatus.PARTIAL) Chip(
                        "Partial",
                        ChipColor.ORANGE
                    )
                    else if (invoice.paymentStatus == PaymentStatus.UNPAID) Chip(
                        "Unpaid",
                        ChipColor.ORANGE
                    )


                }

                if (invoice.isEdited) { //invoice.isEdited
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp) // gap-1
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edited",
                            tint = AxiomTheme.components.card.mutedText,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Edited",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            fontStyle = FontStyle.Italic,
                            color = AxiomTheme.components.card.mutedText
                        )
                    }
                }
            }
        }


    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SupplierCard(
    supplier: PartyEntity,

    totalSpend: Double = 0.0,       // Pass calculated spend from ViewModel
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val isDark = isSystemInDarkTheme()
    val isRegistered = supplier.registrationType.contains("Gst", ignoreCase = true)

    // --- Tailwind Colors ---
    val primaryColor = Color(0xFF3B82F6)
    val surfaceDark = Color(0xFF18181A)
    val appBgDark = Color(0xFF09090B)
    val appBgLight = Color(0xFFF3F4F6)

    val textMain = if (isDark) Color.White else Color(0xFF111827)
    val textMuted = if (isDark) Color(0xFFA1A1AA) else Color(0xFF6B7280)

    val containerBg =
        if (isSelected) primaryColor.copy(alpha = 0.1f) else if (isDark) surfaceDark else Color.White
    val borderColor =
        if (isSelected) primaryColor else if (isDark) Color.White.copy(alpha = 0.05f) else Color(
            0xFFF3F4F6
        )
    val borderWidth = if (isSelected) 2.dp else 1.dp

    // Avatar Colors (Orange for Unregistered, Gray for Registered just like HTML)
    val avatarBg = if (isRegistered) {
        if (isDark) Color.White.copy(alpha = 0.1f) else Color(0xFFF3F4F6)
    } else {
        if (isDark) Color(0xFFF97316).copy(alpha = 0.1f) else Color(0xFFFFF7ED)
    }
    val avatarText =
        if (isRegistered) textMuted else if (isDark) Color(0xFFFB923C) else Color(0xFFEA580C)
    val avatarBorder = if (isRegistered) {
        if (isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFE5E7EB)
    } else {
        if (isDark) Color(0xFFF97316).copy(alpha = 0.2f) else Color(0xFFFFEDD5)
    }

    Box(
        modifier = Modifier
            .padding(bottom = 12.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .background(containerBg)
            .border(borderWidth, borderColor, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {

            // --- Left: Avatar / Logo ---
            Box(modifier = Modifier.padding(end = 16.dp)) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(avatarBg)
                        .border(1.dp, avatarBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    // Fallback to text initials since Logo URL requires Coil/Glide
                    val initials = supplier.businessName.take(2).uppercase()
                    Text(
                        text = initials,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = avatarText
                    )
                }

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 4.dp, y = (-4).dp)
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(primaryColor)
                            .border(2.dp, if (isDark) surfaceDark else Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            // --- Middle: Supplier Details ---
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 2.dp)
                ) {
                    Text(
                        text = supplier.businessName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textMain,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .padding(end = 8.dp)
                    )

                    // GST Registration Tag
                    val tagBg = if (isRegistered) {
                        if (isDark) Color(0xFF22C55E).copy(alpha = 0.1f) else Color(0xFFF0FDF4)
                    } else {
                        if (isDark) Color.White.copy(alpha = 0.1f) else Color(0xFFF3F4F6)
                    }
                    val tagText = if (isRegistered) {
                        if (isDark) Color(0xFF4ADE80) else Color(0xFF16A34A)
                    } else {
                        if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)
                    }

                    Text(
                        text = if (isRegistered) "GST REG" else "UNREG",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = tagText,
                        modifier = Modifier
                            .background(tagBg, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = supplier.gstNumber?.takeIf { it.isNotBlank() }?.let { "GST: $it" }
                        ?: "No GSTIN",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    fontStyle = if (supplier.gstNumber.isNullOrBlank()) FontStyle.Italic else FontStyle.Normal,
                    color = textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )


            }

            // --- Right: Spend Info ---
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(start = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.padding(bottom = 2.dp)
                ) {
                    Text(
                        text = "₹ ${String.format(Locale.US, "%,.0f", totalSpend)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = textMain
                    )
                    Text(
                        text = " Spend",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Normal,
                        color = textMuted,
                        modifier = Modifier.padding(bottom = 1.dp)
                    )
                }

            }
        }
    }
}

@Composable
fun PurchaseRecordCard(
    record: PurchaseRecordEntity,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {


    val borderColor =
        if (isSelected) AxiomTheme.components.card.selectedBorder else AxiomTheme.components.card.border
    val background =
        if (isSelected) AxiomTheme.components.card.selectedBackground else AxiomTheme.components.card.background

    val borderWidth = if (isSelected) 2.dp else 1.dp
    val isDark = isSystemInDarkTheme()

    // --- Tailwind Color Mappings ---
    val primaryColor = Color(0xFF3B82F6) // blue-500
    val appBgDark = Color(0xFF09090B)
    val appBgLight = Color(0xFFF3F4F6)

    val formatter = remember {
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    }

    val dateStr = formatter.format(Date(record.purchaseDate))
    val cardShape = RoundedCornerShape(20.dp)

    Box(
        modifier = modifier
            .padding(bottom = 12.dp)
            .fillMaxWidth()
            .clip(cardShape)
            .background(AxiomTheme.components.card.background)
            .border(1.dp, borderColor, cardShape)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(16.dp)
    ) {
        // --- Main Card Content ---
        Column(
            modifier = Modifier
                .fillMaxWidth()


        ) {

            // Row 1: Supplier Name & Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = record.supplierInvoiceNumber,
                    fontSize = 17.sp, // text-[15px]
                    fontWeight = FontWeight.SemiBold,
                    color = AxiomTheme.components.card.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 16.dp) // pr-4
                )
                Text(
                    text = "₹ ${String.format(Locale.US, "%,.0f", record.grandTotal)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0D9488), // Teal for total amount
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(5.dp))

            // Row 2: Supplier Invoice No & Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp) // gap-1.5
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ShoppingCart, // Updated icon for purchase
                        contentDescription = "Purchase Record",
                        tint = AxiomTheme.components.card.mutedText,
                        modifier = Modifier.size(12.dp) // text-[14px]
                    )
                    Text(
                        // Showing supplier invoice number (fallback to internal ID if blank)
                        text = record.supplierInvoiceNumber.ifBlank { "Ref-${record.id.take(6)}" },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = AxiomTheme.components.card.subtitle
                    )
                }

                Text(
                    text = dateStr,
                    fontSize = 12.sp,
                    color = AxiomTheme.components.card.subtitle
                )
            }

            Spacer(modifier = Modifier.height(11.dp))

            // Row 3: Tags & Edited Status
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp), // pt-1
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp), // gap-2
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Supply Type Tag
                    if (record.supplyType == SupplyType.INTER_STATE) {
                        Chip("Inter-State", ChipColor.GRAY)
                    } else {
                        Chip("Intra-State", ChipColor.GRAY)
                    }

                    // Replaced Invoice statuses with Purchase-specific flags
                    if (record.isItcEligible) {
                        Chip("ITC Eligible", ChipColor.BLUE)
                    }
                    if (record.reverseChargeApplicable) {
                        Chip("Reverse Chg", ChipColor.ORANGE)
                    }
                }

                if (record.isEdited) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp) // gap-1
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edited",
                            tint = AxiomTheme.components.card.mutedText,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Edited",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            fontStyle = FontStyle.Italic,
                            color = AxiomTheme.components.card.mutedText
                        )
                    }
                }
            }
        }

        // --- Absolute Selection Badge ---
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 8.dp, y = (-8).dp) // -top-2 -right-2
                    .size(24.dp) // w-6 h-6
                    .clip(CircleShape)
                    .background(primaryColor)
                    .border(
                        width = 2.dp,
                        color = if (isDark) appBgDark else appBgLight, // Matches the scaffold background
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}


