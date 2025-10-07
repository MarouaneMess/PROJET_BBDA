# Git et versioning recommandé

## Initialisation du repository

```bash
git init
git add .
git commit -m "TP1: Structure initiale du MiniSGBDR"
```

## Structure des commits recommandée

### Convention de nommage
```
<type>: <description courte>

<description détaillée si nécessaire>
```

**Types :**
- `feat`: Nouvelle fonctionnalité
- `fix`: Correction de bug
- `refactor`: Refactoring du code
- `test`: Ajout ou modification de tests
- `docs`: Documentation
- `build`: Modifications du build

**Exemples :**
```
feat: Implémentation de la classe DBConfig
test: Ajout des tests unitaires pour DBConfig
docs: Mise à jour du README avec instructions d'installation
```

## Branches recommandées

```
main            # Version stable
├── develop     # Développement en cours
├── tp2         # Travail spécifique TP2
├── tp3         # Travail spécifique TP3
└── hotfix/*    # Corrections urgentes
```

## .gitignore recommandé

```gitignore
# Java
*.class
*.jar
*.war
*.ear
*.log

# Build directories
build/
target/
out/

# IDE
.vscode/
.idea/
*.iml
.eclipse/

# OS
.DS_Store
Thumbs.db

# Temporary files
*.tmp
*.temp
*.swp
*.swo

# Database files (à adapter selon besoins)
*.db
*.sqlite
*.dat
```

## Workflow Git recommandé

1. **Avant chaque TP :**
   ```bash
   git checkout main
   git pull origin main
   git checkout -b tp2
   ```

2. **Pendant le développement :**
   ```bash
   git add .
   git commit -m "feat: Implémentation du BufferManager"
   ```

3. **À la fin du TP :**
   ```bash
   git checkout main
   git merge tp2
   git push origin main
   ```