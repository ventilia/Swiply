package com.swiply.app.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.swiply.app.core.ui.theme.PillShape
import com.swiply.app.core.ui.theme.swiply

/** Показ интересов read-only (карточка, профиль). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InterestChipsRow(
    interests: List<String>,
    modifier: Modifier = Modifier,
    onSurface: Boolean = false,
) {
    if (interests.isEmpty()) return
    FlowRow(
        modifier = modifier,
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp),
    ) {
        interests.forEach { interest ->
            val bg = if (onSurface) Color.White.copy(alpha = 0.18f) else MaterialTheme.swiply.brandContainer
            val fg = if (onSurface) Color.White else MaterialTheme.swiply.brand
            Text(
                text = interest,
                style = MaterialTheme.typography.labelMedium,
                color = fg,
                modifier = Modifier
                    .background(bg, PillShape)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }
    }
}

/** Выбор интересов (редактор профиля, регистрация). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InterestChipsSelector(
    all: List<String>,
    selected: Set<String>,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
    ) {
        all.forEach { interest ->
            val isSelected = interest in selected
            Text(
                text = interest,
                style = MaterialTheme.typography.labelMedium,
                color = if (isSelected) Color.White else MaterialTheme.swiply.textPrimary,
                modifier = Modifier
                    .background(
                        color = if (isSelected) MaterialTheme.swiply.brand else MaterialTheme.swiply.surface,
                        shape = PillShape,
                    )
                    .border(
                        width = 1.dp,
                        color = if (isSelected) MaterialTheme.swiply.brand else MaterialTheme.swiply.outline,
                        shape = PillShape,
                    )
                    .clickable { onToggle(interest) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            )
        }
    }
}
