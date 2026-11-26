package com.spgrvl.kissos.data.receipt

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.raise.catch
import com.spgrvl.kissos.base.threading.DispatchersProvider
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
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
            when (code) {
                is ReceiptCode.CashRegister -> text(code.lookupUrl).map(AadePage::parse)
                is ReceiptCode.MyData -> text(code.lookupUrl).map(MyDataPage::parse)
                is ReceiptCode.FiscalSign -> get(code.lookupUrl).map { FiscalSignPdf.parse(it.readBytes()) }
                is ReceiptCode.InvoicePortal -> text(code.lookupUrl).map { xml ->
                    MyDataXml.parse(xml).withIssuer(ublSupplier(code.ublUrl))
                }
                is ReceiptCode.SimplyCloud -> text(code.lookupUrl).map(SimplyCloudPage::parse)
                is ReceiptCode.Ilyda -> text(code.lookupUrl).map(IlydaPage::parse)
                is ReceiptCode.Eskap -> text(code.lookupUrl).flatMap { page ->
                    val document = EskapPage.parse(page)
                    if (document == null) {
                        Either.Right(AadeLookup.Unrecognized("Eskap page has no myDATA link"))
                    } else {
                        text(document.myData.lookupUrl).map { MyDataPage.parse(it).withIssuer(document.issuer) }
                    }
                }
            }
        }) { e ->
            Either.Left(e.message ?: "Couldn't reach AADE")
        }
    }

    private suspend fun get(url: String): Either<String, HttpResponse> {
        val response = ktorClient.get().get(url)
        return if (response.status == HttpStatusCode.OK) {
            Either.Right(response)
        } else {
            Either.Left("HTTP ${response.status.value}")
        }
    }

    private suspend fun text(url: String): Either<String, String> = get(url).map { it.bodyAsText(Charsets.UTF_8) }

    /** The name is only a nicety: the document is still found without it. */
    private suspend fun ublSupplier(url: String): Issuer? = catch({
        text(url).getOrNull()?.let(UblInvoice::supplier)
    }) { null }
}
