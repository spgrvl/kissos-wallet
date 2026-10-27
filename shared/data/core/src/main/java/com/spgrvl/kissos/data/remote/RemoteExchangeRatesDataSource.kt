package com.spgrvl.kissos.data.remote

import arrow.core.Either
import com.spgrvl.kissos.data.remote.responses.ExchangeRatesResponse

interface RemoteExchangeRatesDataSource {
    suspend fun fetchEurExchangeRates(): Either<String, ExchangeRatesResponse>
}
