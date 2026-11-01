# Contributing to Kissos

Kissos is a fork of [Ivy Wallet](https://github.com/Ivy-Apps/ivy-wallet), focused on expense tracking and receipt scanning in Greece. Bug reports, unsupported receipts and pull requests are welcome.

## 1. Find or open an issue

Browse the [issues](https://github.com/spgrvl/kissos-wallet/issues), or [open a new one](https://github.com/spgrvl/kissos-wallet/issues/new/choose) if nothing fits. For larger changes, agree on the approach in the issue before you start.

A receipt Kissos can't read can be reported straight from the app.

## 2. Create a feature branch

Fork the repo, then branch from `develop`:

```
git checkout develop
git checkout -b fix/short-description
```

## 3. Make your change

Read the [Developer Guidelines](docs/Guidelines.md) before you begin.

- Keep changes small and focused, with short [Conventional Commits](https://www.conventionalcommits.org/).
- Add English and Greek text together, in `values/strings.xml` and `values-el/strings.xml`.
- Test on a real device or emulator. There is no QA.

## 4. Open a pull request

Open a pull request against the `develop` branch of `spgrvl/kissos-wallet`. `main` only receives `develop`.

CI runs unit tests, integration tests, Detekt, Lint and Paparazzi screenshot tests. If a check fails, see [Troubleshooting CI failures](docs/CI-Troubleshooting.md).

## License

Contributions are released under the [GPL-3.0 license](LICENSE).
