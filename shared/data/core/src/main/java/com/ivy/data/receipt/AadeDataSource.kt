package com.ivy.data.receipt

import arrow.core.Either
import arrow.core.raise.catch
import com.ivy.base.threading.DispatchersProvider
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.withContext
import javax.inject.Inject

/** Looks receipts up on AADE's public pages for them. */
class AadeDataSource @Inject constructor(
    private val ktorClient: dagger.Lazy<HttpClient>,
    private val dispatchers: DispatchersProvider,
) {
    /**
     * @return AADE's answer, or Left when it couldn't be reached (offline, timeout, server error).
     * Left is worth retrying later; every Right is a definite answer for now.
     */
    suspend fun lookup(code: ReceiptCode.OnAade): Either<String, AadeLookup> = withContext(dispatchers.io) {
        catch({
            val response = ktorClient.get().get(code.lookupUrl)
            if (response.status == HttpStatusCode.OK) {
                val html = response.bodyAsText(Charsets.UTF_8)
                Either.Right(
                    when (code) {
                        is ReceiptCode.CashRegister -> AadePage.parse(html)
                        is ReceiptCode.MyData -> MyDataPage.parse(html)
                    }
                )
            } else {
                Either.Left("HTTP ${response.status.value}")
            }
        }) { e ->
            Either.Left(e.message ?: "Couldn't reach AADE")
        }
    }
}
