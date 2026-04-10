-- Script SQL pour augmenter max_allowed_packet dans MySQL
-- Exécutez ce script dans MySQL Workbench ou votre client MySQL

-- Configuration temporaire (perd la valeur au redémarrage de MySQL)
SET GLOBAL max_allowed_packet=10485760; -- 10 MB

-- Vérifier que la configuration a été appliquée
SHOW VARIABLES LIKE 'max_allowed_packet';

-- Vous devriez voir : max_allowed_packet | 10485760
