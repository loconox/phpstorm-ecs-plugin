# Easy Coding Standard for PhpStorm

PhpStorm plugin that integrates [Easy Coding Standard (ECS)](https://github.com/easy-coding-standard/easy-coding-standard) as a native Quality Tool. Get real-time code analysis and automatic formatting directly in your IDE.

## Features

- **Real-time inspections** — ECS violations are highlighted in the editor as you type, just like built-in PHP inspections
- **Code formatting** — Run ECS `--fix` via PhpStorm's *Reformat Code* action (`Cmd+Alt+L` / `Ctrl+Alt+L`)
- **Native integration** — Appears under *Settings > PHP > Quality Tools*, alongside PHP_CodeSniffer and PHP-CS-Fixer
- **Configurable** — Custom tool path, config file path, and execution timeout

## Requirements

- **PhpStorm** 2025.3+
- **ECS** installed in your project (typically via `composer require --dev symplify/easy-coding-standard`)
- **PHP** configured as an interpreter in PhpStorm

## Installation

### From JetBrains Marketplace

1. Open **Settings > Plugins > Marketplace**
2. Search for **Easy Coding Standard**
3. Click **Install**

## Configuration

Go to **Settings > PHP > Quality Tools > Easy Coding Standard**.

| Setting | Default | Description |
|---|---|---|
| Tool path | `vendor/bin/ecs` | Path to the ECS binary (absolute or relative to project root) |
| Configuration file | `ecs.php` | Path to your ECS config (leave empty to use default) |
| Timeout | 30000 ms | Maximum execution time per file |

The plugin automatically resolves relative paths from your project root and detects the PHP interpreter configured in PhpStorm.

## Usage

### Real-time analysis

Once configured, ECS violations appear as warnings in the editor — no action needed. Violations show the checker name and message inline.

The inspection can be toggled at **Settings > Editor > Inspections > PHP > Quality Tools > Easy Coding Standard**.

### Code formatting

Use PhpStorm's standard *Reformat Code* action:
- **macOS**: `Cmd+Alt+L`
- **Windows/Linux**: `Ctrl+Alt+L`

ECS runs with `--fix` on the current file and applies the formatted result.

> **Note**: If you have PhpStorm's built-in *External Formatter* enabled (for PHP_CodeSniffer or PHP-CS-Fixer), the ECS formatter will not interfere — it defers to the active external formatter.

## Building from source

```bash
./gradlew buildPlugin
```

The plugin `.zip` will be in `build/distributions/`.

### Running tests

```bash
./gradlew test
```

### Running in a sandbox IDE

```bash
./gradlew runIde
```

## License

MIT
