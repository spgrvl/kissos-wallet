package com.spgrvl.kissos.data.repository.fake

import com.spgrvl.kissos.base.TestDispatchersProvider
import com.spgrvl.kissos.data.DataObserver
import com.spgrvl.kissos.data.repository.RepositoryMemoFactory
import org.jetbrains.annotations.VisibleForTesting

@VisibleForTesting
fun fakeRepositoryMemoFactory(): RepositoryMemoFactory = RepositoryMemoFactory(
    dataObserver = DataObserver(),
    dispatchers = TestDispatchersProvider
)