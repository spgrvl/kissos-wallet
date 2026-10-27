[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

# Kissos

**Open-source Android expense tracker built for Greece, with AADE receipt scanning. Small amounts, steady growth.**

Kissos (κισσός, Greek for *ivy*) is a personal money manager forked from [Ivy Wallet](https://github.com/Ivy-Apps/ivy-wallet). It keeps everything Ivy Wallet does well (accounts, categories, budgets, planned payments, loans, reports) and adds features for everyday life in Greece.

> [!NOTE]
> Kissos is in early development.

## Made for Greece

- **Scan Greek receipts.** Add an expense by scanning the QR code on a cash-register (ΦΗΜ) receipt, from the add-expense screen, the quick-add menu, or by opening the receipt's link from your camera app. The amount is read offline, and the merchant, date and VAT are filled in from AADE. Receipts scanned offline, or not yet sent to AADE, are checked again in the background.
- **E-invoicing receipts too.** Receipts sent to myDATA are read from AADE when their QR code links there, and from the provider for Epsilon Digital and Fiscal Sign receipts. These need a connection when scanning, since their code holds no amount.
- **Merchant memory.** Remembers your name, category and account for each shop (by ΑΦΜ), so repeat scans take one tap. Manage them under *Settings → Merchants*.
- **In Greek.** The whole app is translated to Greek.

## Planned

- **Better statistics.** Spending trends, spending per merchant, and progress towards the yearly e-payment target.
- **Net worth.** Track investments, property and loans alongside your everyday accounts.

## Privacy

Your data stays on your device. Kissos has no accounts, analytics or tracking. Its only network calls are for exchange rates, the receipt lookups on AADE's site, and the in-app release notes and contributor list from GitHub.

## Moving from Ivy Wallet

Kissos installs alongside Ivy Wallet. To bring your data over, export a backup from Ivy Wallet and import it into Kissos.

## Building

- The latest stable [Android Studio](https://developer.android.com/studio)
- Gradle JDK 21 (*Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK*). JDK 24 or newer doesn't work with the project's Gradle version.

Open the project in Android Studio and run the `app` configuration. See the [developer guidelines](docs/Guidelines.md) for the architecture and conventions.

## Tech stack

- 100% [Kotlin](https://kotlinlang.org/) and [Jetpack Compose](https://developer.android.com/jetpack/compose), with [Material3](https://m3.material.io/)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) and [Flow](https://kotlinlang.org/docs/flow.html)
- [Hilt](https://dagger.dev/hilt/) (DI) and [ArrowKt](https://arrow-kt.io/)
- [Room](https://developer.android.com/training/data-storage/room) and [DataStore](https://developer.android.com/topic/libraries/architecture/datastore) (local storage)
- [Ktor client](https://ktor.io/docs/getting-started-ktor-client.html) and [Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization) (networking)
- [JUnit4](https://github.com/junit-team/junit4), [Kotest](https://kotest.io/) and [Paparazzi](https://github.com/cashapp/paparazzi) (testing)
- [Detekt](https://github.com/detekt/detekt), [Ktlint](https://github.com/pinterest/ktlint) and [Slack's compose-lints](https://slackhq.github.io/compose-lints/) (linting)
- [Gradle KTS](https://docs.gradle.org/current/userguide/kotlin_dsl.html) with convention plugins and version catalogs, and [GitHub Actions](https://docs.github.com/actions) (CI)

## Credits

Kissos is built on [Ivy Wallet](https://github.com/Ivy-Apps/ivy-wallet), created by Ivy Apps and its contributors. The original project stopped being maintained on 5 November 2024 and invited forks under its license. Thank you to everyone who built it.

<a href="https://github.com/Ivy-Apps/ivy-wallet/graphs/contributors">
  <img alt="Ivy Wallet contributors" src="https://contrib.rocks/image?repo=Ivy-Apps/ivy-wallet" />
</a>

Special thanks to Stefan Ilijev, co-founder and designer of Ivy Wallet, who created the [Ivy design system](https://www.figma.com/file/kSwIa07jcHEHZXo6rzx7dn/Design-System?node-id=0%3A1&mode=dev), and to [Aditya](https://github.com/adx69) and [Shymom](https://github.com/SHYMOM) for their creative contributions.

The app's contributors screen also credits Ivy Wallet's contributors.

## License

Kissos is free software under the [GNU General Public License v3.0](LICENSE), the same license as Ivy Wallet. It is provided "as is", without warranty of any kind.
