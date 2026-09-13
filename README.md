# Nexora Manager

**Your ecosystem. Your rules.**

Nexora Manager is an Android patch manager focused on a distinct Nexora experience: a dedicated interface, configurable patch sources, signed release builds, application management, and a guided patching workflow.

## Current status

Nexora Manager is under active development. Development builds are produced by the repository's signed Android build workflow and should be treated as pre-release software until a stable release is published.

## Features

- Browse installed and supported Android applications.
- Load patch bundles from configured sources.
- Select patches and required options.
- Apply, sign, save, and install patched APKs.
- Manage downloaded APKs and downloader integrations.
- Configure update channels, patch safeguards, signing, appearance, and advanced options.
- Review patch logs, changelogs, source information, and update status.

## Project direction

Nexora is being progressively separated from its upstream implementation. Public branding, documentation, release metadata, source infrastructure, namespaces, and technical dependencies are being migrated in controlled stages so validated functionality is not broken by a blind rename.

## Build

The Android project uses Gradle and Java 17. The repository's Nexora workflow builds the release APK, verifies that no GitHub token is embedded, checks the signing certificate and checksum, and uploads the signed APK as a workflow artifact.

See [`docs/`](docs/) for the existing build and usage documentation while it is being migrated to Nexora terminology.

## License and upstream provenance

Nexora Manager is distributed under **GNU GPL v3**. See [`LICENSE`](LICENSE).

This codebase began as a modified GPL-3.0 fork of **ReVanced Manager**. Nexora Manager is an independent project and is not presented as, endorsed by, or affiliated with the ReVanced project. Upstream copyright, license, source-history, and third-party dependency notices are intentionally preserved where required.

Some upstream technical dependencies remain while the independence migration is in progress. Their names in dependency metadata or source-level compatibility code do not represent Nexora branding.

## Contributing

Contributions should target Nexora Manager behavior and documentation. See [`CONTRIBUTING.md`](CONTRIBUTING.md) before opening a pull request.

When reporting a problem, use the repository's Nexora issue templates and include patcher/debug logs when relevant.
