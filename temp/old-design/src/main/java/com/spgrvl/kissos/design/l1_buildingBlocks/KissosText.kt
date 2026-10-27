package com.spgrvl.kissos.design.l1_buildingBlocks

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import com.spgrvl.kissos.design.l1_buildingBlocks.data.KissosPadding
import com.spgrvl.kissos.design.utils.kissosPadding
import com.spgrvl.kissos.design.utils.thenIf

@Deprecated("Old design system. Use `:ivy-design` and Material3")
@Composable
fun KissosText(
    modifier: Modifier = Modifier,
    text: String,
    typo: TextStyle,
    padding: KissosPadding? = null
) {
    Text(
        modifier = Modifier
            .thenIf(padding != null) {
                kissosPadding(kissosPadding = padding!!)
            }
            .then(modifier),
        text = text,
        style = typo,
    )
}
