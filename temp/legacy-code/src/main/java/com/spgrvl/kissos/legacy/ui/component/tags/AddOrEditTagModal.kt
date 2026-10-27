package com.spgrvl.kissos.legacy.ui.component.tags

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import arrow.core.raise.either
import com.spgrvl.kissos.data.model.Tag
import com.spgrvl.kissos.data.model.TagId
import com.spgrvl.kissos.data.model.primitive.NotBlankTrimmedString
import com.spgrvl.kissos.ui.R
import com.spgrvl.kissos.wallet.ui.theme.components.DeleteButton
import com.spgrvl.kissos.wallet.ui.theme.components.KissosTitleTextField
import com.spgrvl.kissos.wallet.ui.theme.modal.KissosModal
import com.spgrvl.kissos.wallet.ui.theme.modal.ModalPositiveButton
import com.spgrvl.kissos.wallet.ui.theme.modal.ModalTitle

@Suppress("DEPRECATION")
@SuppressLint("ComposeModifierMissing")
@ExperimentalFoundationApi
@Composable
fun BoxWithConstraintsScope.AddOrEditTagModal(
    id: TagId,
    @Suppress("UNUSED_PARAMETER") modifier: Modifier = Modifier,
    visible: Boolean = false,
    initialTag: Tag? = null,
    onTagAdd: (String) -> Unit = {},
    onTagEdit: (oldTag: Tag, newTag: Tag) -> Unit = { _, _ -> },
    onTagDelete: (Tag) -> Unit = {},
    onDismiss: () -> Unit
) {
    val titleFocus = FocusRequester()

    var titleTextFieldValue by remember(id) {
        mutableStateOf(
            TextFieldValue(
                initialTag?.name?.value ?: "",
                selection = TextRange(initialTag?.name?.value?.length ?: 0)
            )
        )
    }

    var filename by remember(id) {
        mutableStateOf(initialTag?.name?.value ?: "")
    }

    KissosModal(
        id = id.value,
        visible = visible,
        dismiss = onDismiss,
        PrimaryAction = {
            ModalPositiveButton(
                onClick = {
                    if (initialTag != null) {
                        val updatedTag = either {
                            initialTag.copy(
                                name = NotBlankTrimmedString.from(filename).bind()
                            )
                        }.getOrNull()

                        if (updatedTag != null) {
                            onTagEdit(initialTag, updatedTag)
                        }
                    } else {
                        onTagAdd(filename)
                    }
                    onDismiss()
                },
                text = stringResource(R.string.done),
                iconStart = R.drawable.ic_custom_document_s,
                enabled = filename.isNotEmpty()
            )
        }
    ) {
        Spacer(Modifier.height(32.dp))

        Row(
            modifier = Modifier
                .padding(start = 0.dp, end = 32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ModalTitle(text = stringResource(if (initialTag == null) R.string.new_tag else R.string.edit_tag))

            Spacer(modifier = Modifier.weight(1f))

            if (initialTag != null) {
                DeleteButton(
                    hasShadow = false,
                    onClick = {
                        onTagDelete(initialTag)
                    }
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        KissosTitleTextField(
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .focusRequester(titleFocus),
            dividerModifier = Modifier
                .padding(horizontal = 24.dp),
            value = titleTextFieldValue,
            hint = stringResource(R.string.tag_name),
            keyboardOptions = KeyboardOptions(
                autoCorrect = true,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
                capitalization = KeyboardCapitalization.Sentences
            ),
            keyboardActions = KeyboardActions(
                onNext = {}
            )
        ) {
            titleTextFieldValue = it
            filename = it.text
        }

        LaunchedEffect(titleFocus) {
            titleFocus.requestFocus()
        }
    }
}