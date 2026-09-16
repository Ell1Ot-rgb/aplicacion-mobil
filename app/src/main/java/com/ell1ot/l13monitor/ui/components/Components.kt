package com.ell1ot.l13monitor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ell1ot.l13monitor.ui.theme.L13Ok
import com.ell1ot.l13monitor.ui.theme.L13Warn

@Composable
fun StatusIndicator(ok: Boolean, label: String, modifier: Modifier = Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(if (ok) L13Ok else L13Warn, CircleShape),
        )
        Text(
            text = "  $label",
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
fun MetricRow(label: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        Text(text = value)
    }
}

@Composable
fun CommandButton(text: String, enabled: Boolean = true, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(onClick = onClick, enabled = enabled, modifier = modifier) {
        Text(text)
    }
}

@Composable
fun TopologyCard(title: String, subtitle: String?, content: (@Composable () -> Unit)? = null) {
    Card {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.fillMaxWidth().background(Color.Transparent),
        ) {
            Text(text = title, style = MaterialTheme.typography.titleLarge)
            if (subtitle != null) {
                Text(text = subtitle, style = MaterialTheme.typography.bodyLarge)
            }
            content?.invoke()
        }
    }
}
