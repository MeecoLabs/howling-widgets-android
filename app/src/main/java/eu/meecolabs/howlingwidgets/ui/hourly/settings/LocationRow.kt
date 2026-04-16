package eu.meecolabs.howlingwidgets.ui.hourly.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import eu.meecolabs.howlingwidgets.R
import org.breezyweather.datasharing.BreezyLocation


@Composable
fun LocationRow(
    location: BreezyLocation,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val title = location.city.ifBlank { "Unknown" }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge
        )

        if (location.isCurrentPosition) {
            Icon(
                painter = painterResource(R.drawable.ic_current_location),
                contentDescription = "Current position",
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(Modifier.weight(1f))

        if (isSelected) {
            Icon(
                painter = painterResource(R.drawable.ic_checkmark),
                contentDescription = "Currently selected location",
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
