package com.spgrvl.kissos.design.api

import com.spgrvl.kissos.base.legacy.Theme
import com.spgrvl.kissos.design.KissosContext
import com.spgrvl.kissos.design.l0_system.KissosColors
import com.spgrvl.kissos.design.l0_system.KissosShapes
import com.spgrvl.kissos.design.l0_system.KissosTypography

@Deprecated("Old design system. Use `:ivy-design` and Material3")
interface KissosDesign {
    @Deprecated("Old design system. Use `:ivy-design` and Material3")
    fun context(): KissosContext

    @Deprecated("Old design system. Use `:ivy-design` and Material3")
    fun typography(): KissosTypography

    @Deprecated("Old design system. Use `:ivy-design` and Material3")
    fun colors(theme: Theme, isDarkModeEnabled: Boolean): KissosColors

    @Deprecated("Old design system. Use `:ivy-design` and Material3")
    fun shapes(): KissosShapes
}
