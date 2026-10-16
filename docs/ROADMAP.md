# Roadmap

## Milestone 0 — UI starter
- [x] App shell and tabs
- [x] Demo command runner with allowlist
- [x] Linux/package backend status clearly disclosed

## Milestone 1 — real terminal
- [ ] Choose terminal view/PTY implementation after license review
- [ ] Add interactive session lifecycle and resize support
- [ ] Add terminal input method/extra keys and selection/copy
- [ ] Add output buffering and process cancellation

## Milestone 2 — Linux runtime
- [ ] Select and audit a maintained Android-compatible PRoot build
- [ ] Implement verified ARM64 rootfs download
- [ ] Safe extraction, disk checks, cancellation, cleanup
- [ ] Debian launch environment and persistent home
- [ ] Integration tests on Android 16

## Milestone 3 — package manager
- [ ] apt update/search/install/remove only inside Debian
- [ ] Show logs, exit codes, and cancellation
- [ ] Prevent simultaneous apt operations

## Milestone 4 — quality
- [ ] App settings and theme persistence
- [ ] Export diagnostic logs without secrets
- [ ] Accessibility, keyboard navigation, crash recovery
- [ ] Release signing and reproducible build documentation
