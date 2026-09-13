package app.revanced.manager.ui.screen

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TwoRowsTopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.children
import app.revanced.manager.R
import app.revanced.manager.network.dto.ProjectAnnouncement
import app.revanced.manager.ui.component.NexoraOfficialCyan
import app.revanced.manager.ui.component.NexoraOfficialMuted
import app.revanced.manager.ui.component.NexoraOfficialText
import app.revanced.manager.ui.component.NexoraPageScaffold
import app.revanced.manager.ui.component.TooltipIconButton
import app.revanced.manager.util.relativeTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.intellij.lang.annotations.Language

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AnnouncementScreen(
    onBackClick: () -> Unit,
    announcement: ProjectAnnouncement
) {
    val scrollState = rememberScrollState()
    val createDate = announcement.createdAt.toLocalDateTime(TimeZone.UTC).relativeTime(LocalContext.current)
    val headerTextColor = NexoraOfficialText
    val textColor = NexoraOfficialMuted
    val linkColor = NexoraOfficialCyan

    NexoraPageScaffold(
        title = announcement.title,
        subtitle = "$createDate\u2002\u2022\u2002${announcement.author}",
        onBackClick = onBackClick,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(paddingValues)
        ) {
            AnnouncementTag(
                tags = announcement.tags,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            )
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                factory = {
                    val webView = WebView(it).apply {
                        setBackgroundColor(0)
                        isVerticalScrollBarEnabled = false
                        isHorizontalScrollBarEnabled = false
                        isLongClickable = false
                        setOnLongClickListener { true }
                        isHapticFeedbackEnabled = false

                        // Disable WebView's internal scrolling
                        @SuppressLint("ClickableViewAccessibility")
                        setOnTouchListener { _, event ->
                            event.action == MotionEvent.ACTION_MOVE
                        }
                    }
                    FrameLayout(it).apply {
                        addView(webView)
                    }
                },
                update = {
                    val webView = it.children.first() as WebView
                    @Language("HTML")
                    val style = """
                    <html lang="en">
                      <head>
                        <meta name="viewport" content="width=device-width, initial-scale=1" />
                        <style>
                          body {
                            line-height: 150%;
                            color: ${textColor.toCss()};
                          }
                          ul, ol {
                            padding-inline-start: 12px;
                          }
                          strong, b {
                            font-weight: 600;
                            color: ${headerTextColor.toCss()};
                          }
                          h1, h2, h3, h4, h5, h6 {
                            font-weight: 475;
                            line-height: 133%;
                            color: ${headerTextColor.toCss()};
                          }
                          h1 {
                            font-size: 2.25em;
                          }
                          h2 {
                            font-size: 1.75em;
                          }
                          h3 {
                            font-size: 1.5em;
                          }
                          a {
                            color: ${linkColor.toCss()};
                          }
                        </style>
                      </head>
                      <body>
                        ${announcement.content}
                      </body>
                    </html>
                """.trimIndent()
                    webView.loadDataWithBaseURL(null, style, "text/html", "UTF-8", null)
                },
                onRelease = {
                    val webView = it.children.first() as WebView
                    webView.destroy()
                }
            )
        }
    }
}

private fun Color.toCss(): String {
    return "rgba(${red * 255f}, ${green * 255f}, ${blue * 255f}, $alpha)"
}
