# Configuration MySQL pour les grandes images

## Problème
L'erreur indique que votre image (9.5 MB) dépasse la limite par défaut de MySQL (`max_allowed_packet = 1 MB`).

## Solution : Augmenter max_allowed_packet

### Option 1 : Configuration temporaire (recommandé pour tester rapidement)

1. Ouvrez MySQL Workbench ou votre client MySQL préféré
2. Connectez-vous à votre serveur MySQL
3. Exécutez cette commande :

```sql
SET GLOBAL max_allowed_packet=10485760; -- 10 MB
```

**Note :** Cette configuration est temporaire et sera perdue au redémarrage de MySQL.

### Option 2 : Configuration permanente (recommandé pour la production)

#### Sur Windows :

1. **Trouvez le fichier de configuration MySQL** :
   - Par défaut : `C:\ProgramData\MySQL\MySQL Server 8.0\my.ini` (ou version similaire)
   - Ou cherchez `my.ini` ou `my.cnf` dans le dossier d'installation MySQL

2. **Ouvrez le fichier avec les droits administrateur** :
   - Clic droit → "Ouvrir avec" → Bloc-notes (en tant qu'administrateur)

3. **Cherchez la section `[mysqld]`** et ajoutez/modifiez cette ligne :
   ```ini
   [mysqld]
   max_allowed_packet=10485760
   ```
   (10485760 = 10 MB en bytes)

4. **Sauvegardez le fichier**

5. **Redémarrez le service MySQL** :
   - Ouvrez "Services" (Win + R, tapez `services.msc`)
   - Trouvez "MySQL80" (ou votre version)
   - Clic droit → Redémarrer

#### Sur Linux/Mac :

1. **Trouvez le fichier de configuration** :
   ```bash
   mysql --help | grep "Default options" -A 1
   ```
   Généralement : `/etc/mysql/my.cnf` ou `/etc/my.cnf`

2. **Éditez le fichier** :
   ```bash
   sudo nano /etc/mysql/my.cnf
   ```

3. **Ajoutez/modifiez dans la section `[mysqld]`** :
   ```ini
   [mysqld]
   max_allowed_packet=10485760
   ```

4. **Redémarrez MySQL** :
   ```bash
   sudo systemctl restart mysql
   # ou
   sudo service mysql restart
   ```

## Vérifier la configuration

Après avoir configuré, vérifiez que la valeur a bien été appliquée :

```sql
SHOW VARIABLES LIKE 'max_allowed_packet';
```

Vous devriez voir une valeur d'au moins 10485760 (10 MB).

## Valeurs recommandées

- **Pour le développement** : 10 MB (10485760 bytes) - suffisant pour la plupart des images
- **Pour la production** : 16 MB (16777216 bytes) ou plus selon vos besoins
- **Maximum** : 1 GB (1073741824 bytes) - attention à la mémoire disponible

## Alternative : Réduire la taille des images

Si vous préférez ne pas modifier MySQL, vous pouvez :

1. **Compresser les images avant l'upload** (utilisez un outil comme TinyPNG, ImageOptim, etc.)
2. **Redimensionner les images** (les images de chambres n'ont pas besoin d'être en très haute résolution)
3. **Stocker les images sur le système de fichiers** au lieu de la base de données (meilleure pratique)

## Note importante

Après avoir modifié `max_allowed_packet`, **redémarrez toujours votre application Spring Boot** pour que les changements prennent effet.
