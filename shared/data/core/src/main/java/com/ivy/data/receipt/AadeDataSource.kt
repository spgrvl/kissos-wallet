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

/** Looks receipts up on AADE's public verification page (www1.aade.gr). */
class AadeDataSource @Inject constructor(
    private val ktorClient: dagger.Lazy<HttpClient>,
    private val dispatchers: DispatchersProvider,
) {
    /**
     * @return AADE's answer, or Left when it couldn't be reached (offline, timeout, server error).
     * Left is worth retrying later; every Right is a definite answer for now.
     */
    suspend fun lookup(qr: AadeQr): Either<String, AadeLookup> = withContext(dispatchers.io) {
        catch({
            val response = ktorClient.get().get(qr.lookupUrl)
            if (response.status == HttpStatusCode.OK) {
                Either.Right(AadePage.parse(response.bodyAsText(Charsets.UTF_8)))
            } else {
                Either.Left("HTTP ${response.status.value}")
            }
        }) { e ->
            Either.Left(e.message ?: "Couldn't reach AADE")
        }
    }
}
