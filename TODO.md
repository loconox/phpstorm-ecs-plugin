# TODO — PhpStorm ECS Plugin

## Setup initial

- [ ] Générer le Gradle wrapper : `cd phpstorm-ecs-plugin && gradle wrapper`
- [ ] Ouvrir le projet dans IntelliJ IDEA (avec le plugin "Plugin DevKit")
- [ ] Compiler le plugin : `./gradlew buildPlugin`
- [ ] Tester avec une instance PhpStorm : `./gradlew runIde`

## Développement

- [ ] Affiner `EcsMessageProcessor` — adapter le parsing à la sortie réelle d'ECS (`ecs check --output-format=json`)
- [ ] Ajuster les signatures des méthodes si nécessaire selon la version du SDK PHP utilisée
- [ ] Ajouter la gestion du chemin vers le fichier de config ECS (`ecs.php`) dans le formulaire
- [ ] Gérer la détection automatique du binaire ECS (`vendor/bin/ecs`)
- [ ] Ajouter le support des quick-fixes (auto-fix via `ecs check --fix`)

## Tests

- [ ] Écrire des tests unitaires pour `EcsMessageProcessor`
- [ ] Tester l'intégration avec différentes versions de PhpStorm

## Distribution

- [ ] Publier sur le JetBrains Marketplace
