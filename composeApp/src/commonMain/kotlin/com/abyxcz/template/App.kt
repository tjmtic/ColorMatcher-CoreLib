package com.abyxcz.template

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abyxcz.template.resources.Res
import com.abyxcz.template.resources.greeting
import com.abyxcz.template.shared.Greeter
import org.jetbrains.compose.resources.stringResource

/** The whole app's UI, shared by Android and iOS; [platform] names the host. */
@Composable
fun App(platform: String, modifier: Modifier = Modifier) {
    MaterialTheme {
        Surface(modifier = modifier.fillMaxSize()) {
            Text(
                text = stringResource(Res.string.greeting, Greeter().nameFor(platform)),
                style = MaterialTheme.typography.headlineSmall,
                // Edge to edge on both platforms: keep content out of the status bar, notch/
                // Dynamic Island and gesture areas.
                modifier = Modifier.safeDrawingPadding().padding(24.dp),
            )
        }
    }
}
