package com.spgrvl.kissos.loans

import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import com.spgrvl.kissos.loans.loan.LoanScreenUiTest
import com.spgrvl.kissos.ui.testing.PaparazziScreenshotTest
import com.spgrvl.kissos.ui.testing.PaparazziTheme
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
class LoanScreenPaparazziTest(
    @TestParameter
    private val theme: PaparazziTheme,
) : PaparazziScreenshotTest() {
    @Test
    fun `snapshot loanScreen composable`() {
        snapshot(theme) {
            LoanScreenUiTest(theme == PaparazziTheme.Dark)
        }
    }
}