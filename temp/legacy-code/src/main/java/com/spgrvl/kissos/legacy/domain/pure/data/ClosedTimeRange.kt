package com.spgrvl.kissos.wallet.domain.pure.data

import com.spgrvl.kissos.base.time.TimeProvider
import com.spgrvl.kissos.legacy.utils.ivyMinTime
import java.time.Instant

data class ClosedTimeRange(
    val from: Instant,
    val to: Instant,
) {
    companion object {
        fun allTimeIvy(
            timeProvider: TimeProvider,
        ): ClosedTimeRange = ClosedTimeRange(
            from = ivyMinTime(),
            to = timeProvider.utcNow(),
        )

        fun to(to: Instant): ClosedTimeRange = ClosedTimeRange(
            from = ivyMinTime(),
            to = to
        )
    }
}
