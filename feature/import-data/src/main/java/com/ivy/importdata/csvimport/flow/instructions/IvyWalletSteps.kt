package com.ivy.importdata.csvimport.flow.instructions

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ivy.ui.R

@Composable
fun IvyWalletSteps(
    onUploadClick: () -> Unit
) {
    Spacer(Modifier.height(12.dp))

    StepTitle(
        number = 1,
        title = stringResource(R.string.export_data),
        description = stringResource(
            R.string.ivy_backup_step,
            stringResource(R.string.settings),
            stringResource(R.string.backup_data),
            stringResource(R.string.export_to_csv),
        )
    )

    Spacer(Modifier.height(12.dp))

    VideoArticleRow(
        videoUrl = null,
        articleUrl = null
    )

    Spacer(Modifier.height(24.dp))

    UploadFileStep(
        stepNumber = 2,
        text = stringResource(R.string.upload_backup_step),
        btnTitle = stringResource(R.string.upload_backup_button),
        onUploadClick = onUploadClick
    )
}
