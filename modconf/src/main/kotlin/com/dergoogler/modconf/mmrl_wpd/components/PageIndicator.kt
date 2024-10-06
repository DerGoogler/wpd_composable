package com.dergoogler.modconf.mmrl_wpd.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PageIndicator(
    text: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    minHeight: Dp? = null
) = Column(
    modifier = modifier
            then(if (minHeight != null) {
        Modifier
            .defaultMinSize(minHeight = minHeight)
            .fillMaxWidth()
    } else {
        Modifier.fillMaxSize()
    }),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
) {
    Spacer(modifier = Modifier.height(20.dp))
    ProvideTextStyle(value = PageIndicatorDefaults.textStyle) {
        text()
    }
}

@Composable
fun PageIndicator(
    text: String,
    modifier: Modifier = Modifier,
    minHeight: Dp? = null
) = PageIndicator(
    modifier = modifier,
    text = {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 20.dp),
            maxLines = 5,
            overflow = TextOverflow.Ellipsis
        )
    },
    minHeight = minHeight
)

@Composable
fun PageIndicator(
    @DrawableRes icon: Int,
    @StringRes text: Int,
    modifier: Modifier = Modifier,
    minHeight: Dp? = null
) = PageIndicator(
    modifier = modifier,
    text = stringResource(id = text),
    minHeight = minHeight
)


object PageIndicatorDefaults {
    val iconSize = 80.dp
    val iconColor @Composable get() = MaterialTheme.colorScheme.outline.copy(0.5f)

    val textStyle @Composable get() = TextStyle(
        color = MaterialTheme.colorScheme.outline.copy(0.5f),
        fontSize = 20.sp,
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center
    )
}