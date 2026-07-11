package com.swiply.app.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.swiply.app.core.ui.theme.swiply

/**
 * Полноразмерная галерея фото с индикаторами-полосками (как в сторис).
 */
@Composable
fun PhotoPager(
    photoUrls: List<String>,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    if (photoUrls.isEmpty()) {
        Box(
            modifier = modifier.background(MaterialTheme.swiply.brandGradient),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = contentDescription.take(1).uppercase(),
                style = MaterialTheme.typography.displayLarge,
                color = Color.White,
            )
        }
        return
    }

    val pagerState = rememberPagerState(pageCount = { photoUrls.size })
    Box(modifier = modifier) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            AsyncImage(
                model = photoUrls[page],
                contentDescription = "$contentDescription, фото ${page + 1}",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.swiply.surfaceElevated),
            )
        }
        if (photoUrls.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                repeat(photoUrls.size) { index ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(3.dp)
                            .background(
                                color = if (index == pagerState.currentPage) {
                                    Color.White
                                } else {
                                    Color.White.copy(alpha = 0.4f)
                                },
                                shape = RoundedCornerShape(2.dp),
                            ),
                    )
                }
            }
        }
    }
}
