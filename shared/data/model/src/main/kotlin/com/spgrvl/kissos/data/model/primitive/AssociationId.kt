package com.spgrvl.kissos.data.model.primitive

import com.spgrvl.kissos.data.model.sync.UniqueId
import java.util.UUID

@JvmInline
value class AssociationId(override val value: UUID) : UniqueId