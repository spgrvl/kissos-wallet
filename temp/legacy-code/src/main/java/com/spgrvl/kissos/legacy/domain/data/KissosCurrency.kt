package com.spgrvl.kissos.wallet.domain.data

import android.icu.util.Currency
import androidx.compose.runtime.Immutable
import com.spgrvl.kissos.legacy.utils.getDefaultFIATCurrency

@Immutable
data class KissosCurrency(
    val code: String,
    val name: String,
    val isCrypto: Boolean
) {
    companion object {
        private const val CRYPTO_DECIMAL = 18
        private const val FIAT_DECIMAL = 2

        private val CRYPTO = setOf(
            KissosCurrency(
                code = "BTC",
                name = "Bitcoin",
                isCrypto = true
            ),
            KissosCurrency(
                code = "ETH",
                name = "Ethereum",
                isCrypto = true
            ),
            KissosCurrency(
                code = "USDT",
                name = "Tether USD",
                isCrypto = true
            ),
            KissosCurrency(
                code = "BNB",
                name = "Binance Coin",
                isCrypto = true
            ),
            KissosCurrency(
                code = "ADA",
                name = "Cardano",
                isCrypto = true
            ),
            KissosCurrency(
                code = "XRP",
                name = "Ripple",
                isCrypto = true
            ),
            KissosCurrency(
                code = "DOGE",
                name = "Dogecoin",
                isCrypto = true
            ),
            KissosCurrency(
                code = "USDC",
                name = "USD Coin",
                isCrypto = true
            ),
            KissosCurrency(
                code = "DOT",
                name = "Polkadot",
                isCrypto = true
            ),
            KissosCurrency(
                code = "UNI",
                name = "Uniswap",
                isCrypto = true
            ),
            KissosCurrency(
                code = "BUSD",
                name = "Binance USD",
                isCrypto = true
            ),
            KissosCurrency(
                code = "BCH",
                name = "Bitcoin Cash",
                isCrypto = true
            ),
            KissosCurrency(
                code = "SOL",
                name = "Solana",
                isCrypto = true
            ),
            KissosCurrency(
                code = "LTC",
                name = "Litecoin",
                isCrypto = true
            ),
            KissosCurrency(
                code = "LINK",
                name = "ChainLink Token",
                isCrypto = true
            ),
            KissosCurrency(
                code = "SHIB",
                name = "Shiba Inu coin",
                isCrypto = true
            ),
            KissosCurrency(
                code = "LUNA",
                name = "Terra",
                isCrypto = true
            ),
            KissosCurrency(
                code = "AVAX",
                name = "Avalanche",
                isCrypto = true
            ),
            KissosCurrency(
                code = "MATIC",
                name = "Polygon",
                isCrypto = true
            ),
            KissosCurrency(
                code = "CRO",
                name = "Cronos",
                isCrypto = true
            ),
            KissosCurrency(
                code = "WBTC",
                name = "Wrapped Bitcoin",
                isCrypto = true
            ),
            KissosCurrency(
                code = "ALGO",
                name = "Algorand",
                isCrypto = true
            ),
            KissosCurrency(
                code = "XLM",
                name = "Stellar",
                isCrypto = true
            ),
            KissosCurrency(
                code = "MANA",
                name = "Decentraland",
                isCrypto = true
            ),
            KissosCurrency(
                code = "AXS",
                name = "Axie Infinity",
                isCrypto = true
            ),
            KissosCurrency(
                code = "DAI",
                name = "Dai",
                isCrypto = true
            ),
            KissosCurrency(
                code = "ICP",
                name = "Internet Computer",
                isCrypto = true
            ),
            KissosCurrency(
                code = "ATOM",
                name = "Cosmos",
                isCrypto = true
            ),
            KissosCurrency(
                code = "FIL",
                name = "Filecoin",
                isCrypto = true
            ),
            KissosCurrency(
                code = "ETC",
                name = "Ethereum Classic",
                isCrypto = true
            ),
            KissosCurrency(
                code = "DASH",
                name = "Dash",
                isCrypto = true
            ),
            KissosCurrency(
                code = "TRX",
                name = "Tron",
                isCrypto = true
            ),
            KissosCurrency(
                code = "TON",
                name = "Tonchain",
                isCrypto = true
            ),
        )

        fun getAvailable(): List<KissosCurrency> {
            return Currency.getAvailableCurrencies()
                .map {
                    KissosCurrency(
                        code = it.currencyCode,
                        name = it.displayName,
                        isCrypto = false
                    )
                }
                .plus(CRYPTO)
        }

        fun fromCode(code: String): KissosCurrency? {
            if (code.isBlank()) return null

            val crypto = CRYPTO.find { it.code == code }
            if (crypto != null) {
                return crypto
            }

            return try {
                val fiat = Currency.getInstance(code)
                KissosCurrency(
                    fiatCurrency = fiat
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        fun getDefault(): KissosCurrency = KissosCurrency(
            fiatCurrency = getDefaultFIATCurrency()
        )

        fun getDecimalPlaces(assetCode: String): Int =
            if (fromCode(assetCode) in CRYPTO) CRYPTO_DECIMAL else FIAT_DECIMAL
    }

    constructor(fiatCurrency: Currency) : this(
        code = fiatCurrency.currencyCode,
        name = fiatCurrency.displayName,
        isCrypto = false
    )
}
