package com.spgrvl.kissos.design.api

import com.spgrvl.kissos.base.legacy.Theme
import com.spgrvl.kissos.design.IvyContext
import com.spgrvl.kissos.design.l0_system.IvyColors
import com.spgrvl.kissos.design.l0_system.IvyShapes
import com.spgrvl.kissos.design.l0_system.IvyTypography

@Deprecated("Old design system. Use `:ivy-design` and Material3")
interface IvyDesign {
    @Deprecated("Old design system. Use `:ivy-design` and Material3")
    fun context(): IvyContext

    @Deprecated("Old design system. Use `:ivy-design` and Material3")
    fun typography(): IvyTypography

    @Deprecated("Old design system. Use `:ivy-design` and Material3")
    fun colors(theme: Theme, isDarkModeEnabled: Boolean): IvyColors

    @Deprecated("Old design system. Use `:ivy-design` and Material3")
    fun shapes(): IvyShapes
}
