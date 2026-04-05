package com.evangelidisapps.ttworkoutlog.ui.theme

import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindowProvider

private val TOOLBAR_HEIGHT = 64.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    content: @Composable ColumnScope.() -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        dragHandle = {
            Box(modifier = Modifier.fillMaxWidth()) {
                BottomSheetDefaults.DragHandle(modifier = Modifier.align(Alignment.Center))
                IconButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close"
                    )
                }
            }
        }
    ) {
        val dialogView = LocalView.current
        val density = LocalDensity.current
        val statusBarHeightPx = WindowInsets.statusBars.getTop(density)
        val toolbarHeightPx = with(density) { TOOLBAR_HEIGHT.roundToPx() }
        val topOffset = statusBarHeightPx + toolbarHeightPx

        SideEffect {
            val window = (dialogView.parent as? DialogWindowProvider)?.window
            window?.let { win ->
                val displayHeight = dialogView.context.resources.displayMetrics.heightPixels
                val lp = win.attributes
                lp.height = displayHeight - topOffset
                lp.gravity = Gravity.BOTTOM
                lp.width = WindowManager.LayoutParams.MATCH_PARENT
                win.attributes = lp
            }
        }

        content()
    }
}
