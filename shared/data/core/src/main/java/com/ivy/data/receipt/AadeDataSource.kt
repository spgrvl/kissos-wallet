package com.ivy.data.receipt

import arrow.core.Either
import arrow.core.raise.catch
import com.ivy.base.threading.DispatchersProvider
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.readBytes
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.withContext
import javax.inject.Inject

/** Looks receipts up on AADE's public pages for them, or downloads them from their provider. */
class AadeDataSource @Inject constructor(
    private val ktorClient: dagger.Lazy<HttpClient>,
    private val dispatchers: DispatchersProvider,
) {
    /**
     * @return AADE's answer, or Left when it couldn't be reached (offline, timeout, server error).
     * Left is worth retrying later; every Right is a definite answer for now.
     */
    suspend fun lookup(code: ReceiptCode.Fetchable): Either<String, AadeLookup> = withContext(dispatchers.io) {
        catch({
            val response = ktorClient.get().get(code.lookupUrl)
            if (response.status == HttpStatusCode.OK) {
                Either.Right(
                    when (code) {
                        is ReceiptCode.CashRegister -> AadePage.parse(response.bodyAsText(Charsets.UTF_8))
                        is ReceiptCode.MyData -> MyDataPage.parse(response.bodyAsText(Charsets.UTF_8))
                        is ReceiptCode.FiscalSign -> FiscalSignPdf.parse(response.readBytes())
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
