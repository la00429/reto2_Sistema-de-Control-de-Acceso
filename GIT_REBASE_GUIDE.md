# Guía de Rebase en Git

## ¿Qué es un Rebase?

El rebase te permite tomar tus cambios locales y "re-aplicarlos" sobre los cambios del remoto, creando un historial más limpio y lineal.

## Rebase: Integrar Cambios Remotos Sobre los Locales

Si quieres que los cambios del remoto tengan prioridad y que tus cambios se apliquen sobre ellos:

### Paso 1: Obtener los últimos cambios del remoto

```bash
git fetch origin
```

Esto descarga los cambios del remoto sin modificar tu rama local.

### Paso 2: Ver qué cambios hay

```bash
# Ver diferencias entre tu rama y la remota
git log HEAD..origin/main --oneline

# O si tu rama es diferente (ej: master, develop)
git log HEAD..origin/master --oneline
```

### Paso 3: Hacer el Rebase

```bash
# Si estás en la rama main
git rebase origin/main

# Si estás en otra rama (ej: master, develop, feature)
git rebase origin/master
git rebase origin/develop
```

**¿Qué hace esto?**
- Toma tus commits locales
- Los "pausa" temporalmente
- Aplica los cambios del remoto
- Re-aplica tus commits sobre los cambios del remoto
- Crea un historial lineal y limpio

### Paso 4: Resolver Conflictos (si los hay)

Si hay conflictos durante el rebase:

```bash
# Git te mostrará qué archivos tienen conflictos
# Edita los archivos para resolverlos

# Después de resolver cada conflicto:
git add <archivo-resuelto>

# Continúa el rebase
git rebase --continue

# O si prefieres cancelar el rebase:
git rebase --abort
```

### Paso 5: Forzar el Push (si es necesario)

**⚠️ IMPORTANTE**: Después de un rebase, necesitas hacer force push porque reescribiste el historial.

```bash
# Verifica primero que todo esté bien
git status

# Hace force push (usa con cuidado)
git push --force origin <nombre-de-tu-rama>

# O más seguro (force with lease)
git push --force-with-lease origin <nombre-de-tu-rama>
```

## Flujo Completo Ejemplo

```bash
# 1. Asegúrate de estar en tu rama
git checkout main
# o
git checkout feature/mi-feature

# 2. Obtén los últimos cambios
git fetch origin

# 3. Haz el rebase
git rebase origin/main

# 4. Si hay conflictos, resuélvelos
# Edita los archivos conflictivos
git add .
git rebase --continue

# 5. Verifica que todo esté bien
git log --oneline -10

# 6. Push con force
git push --force-with-lease origin main
```

## Alternativa: Merge (Más Seguro)

Si prefieres una opción más segura que no reescriba el historial:

### Merge: Integrar Cambios Remotos

```bash
# 1. Obtener cambios remotos
git fetch origin

# 2. Hacer merge (preserva ambos historiales)
git merge origin/main

# 3. Resolver conflictos si los hay
# Edita archivos conflictivos
git add .
git commit

# 4. Push normal (sin force)
git push origin main
```

## Comparación: Rebase vs Merge

### Rebase (Historial Limpio)
```
Antes:
remoto: A---B---C
local:       D---E

Después del rebase:
remoto: A---B---C
local:           D'---E'  (re-aplicados sobre C)
```

**Ventajas:**
- ✅ Historial más limpio y lineal
- ✅ Más fácil de entender
- ✅ Sin commits de merge innecesarios

**Desventajas:**
- ⚠️ Reescribe el historial
- ⚠️ Requiere force push
- ⚠️ Puede ser más complicado si hay conflictos

### Merge (Preserva Historial)
```
Antes:
remoto: A---B---C
local:       D---E

Después del merge:
remoto: A---B---C
local:       D---E---M  (merge commit)
```

**Ventajas:**
- ✅ No reescribe el historial
- ✅ Push normal (sin force)
- ✅ Preserva el contexto de ambos cambios

**Desventajas:**
- ⚠️ Crea commits de merge
- ⚠️ Historial más "sucio"
- ⚠️ Puede ser confuso con muchos branches

## Escenarios Comunes

### Escenario 1: Quiero actualizar mi rama con cambios remotos

```bash
git fetch origin
git rebase origin/main
git push --force-with-lease origin main
```

### Escenario 2: Estoy trabajando en una feature branch

```bash
# Desde tu feature branch
git checkout feature/mi-feature
git fetch origin
git rebase origin/main
# Resolver conflictos si los hay
git push --force-with-lease origin feature/mi-feature
```

### Escenario 3: Quiero descartar mis cambios locales y usar solo los remotos

```bash
# ⚠️ CUIDADO: Esto elimina todos tus cambios locales
git fetch origin
git reset --hard origin/main
```

### Escenario 4: Quiero guardar mis cambios antes de hacer rebase

```bash
# Guardar cambios en un stash
git stash

# Hacer el rebase
git fetch origin
git rebase origin/main

# Recuperar tus cambios
git stash pop

# Resolver conflictos si los hay
# Luego commit y push
```

## Comandos Útiles

### Ver el estado del rebase

```bash
# Ver en qué commit estás durante el rebase
git status

# Ver el log durante el rebase
git log --oneline --all --graph
```

### Cancelar un rebase

```bash
# Si algo salió mal, puedes cancelar
git rebase --abort
```

### Ver diferencias

```bash
# Ver qué cambios tiene el remoto que tú no tienes
git log HEAD..origin/main

# Ver qué cambios tienes tú que el remoto no tiene
git log origin/main..HEAD

# Ver diferencias en archivos
git diff origin/main
```

## Consejos Importantes

1. **Nunca hagas rebase en ramas compartidas** (main, master, develop) si otros están trabajando en ellas
2. **Usa `--force-with-lease` en lugar de `--force`** para evitar sobrescribir trabajo de otros
3. **Siempre haz `git fetch` primero** para ver qué cambios hay antes de hacer rebase
4. **Si trabajas en equipo**, coordínate antes de hacer rebase en ramas compartidas
5. **En ramas personales (feature branches)**, el rebase está bien y es recomendado

## Resolución de Conflictos Paso a Paso

Durante el rebase, si hay conflictos:

```bash
# Git te dirá qué archivos tienen conflictos
# 1. Abre los archivos y busca los marcadores:
<<<<<<< HEAD
tu cambio
=======
cambio del remoto
>>>>>>> commit-remoto

# 2. Edita para resolver el conflicto
# Elimina los marcadores y deja el código correcto

# 3. Marca como resuelto
git add archivo-resuelto.java

# 4. Continúa el rebase
git rebase --continue

# Repite hasta que no haya más conflictos
```

## Ejemplo Visual Completo

```bash
# Estado inicial
$ git log --oneline --graph
* a1b2c3d (HEAD -> main) Mi cambio local
* e4f5g6h Cambio anterior local
* i7j8k9l Cambio más antiguo
| 
* m1n2o3p (origin/main) Cambio remoto nuevo
* p4q5r6s Cambio remoto anterior
* s7t8u9v Cambio remoto antiguo

# Después de fetch y rebase
$ git fetch origin
$ git rebase origin/main

# Estado después del rebase
$ git log --oneline --graph
* a1b2c3d' (HEAD -> main) Mi cambio local (re-aplicado)
* e4f5g6h' Cambio anterior local (re-aplicado)
* m1n2o3p Cambio remoto nuevo
* p4q5r6s Cambio remoto anterior
* s7t8u9v Cambio remoto antiguo

# Ahora tienes un historial lineal con los cambios remotos primero
```

## Resumen Rápido

**Para hacer rebase con cambios remotos:**

```bash
git fetch origin                    # Obtener cambios
git rebase origin/main             # Aplicar tus cambios sobre los remotos
# Resolver conflictos si los hay
git push --force-with-lease        # Publicar cambios
```

**¿Necesitas ayuda con algo específico?** Puedo ayudarte con tu caso particular.

