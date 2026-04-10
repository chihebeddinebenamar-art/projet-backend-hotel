# Guide détaillé — Tester les APIs avec Postman (projectPfa-hotel)

Ce document décrit **chaque endpoint** du backend Spring Boot : méthode HTTP, URL complète, en-têtes, type de corps (body), paramètres, exemples de requête/réponse et dépannage.

---

## Table des matières

1. [Prérequis et démarrage](#1-prérequis-et-démarrage)
2. [Configurer Postman (environnement + variables)](#2-configurer-postman-environnement--variables)
3. [JWT : quand envoyer le token ?](#3-jwt--quand-envoyer-le-token)
4. [Tableau récapitulatif des endpoints](#4-tableau-récapitulatif-des-endpoints)
5. [API Authentification (`/api/auth`)](#5-api-authentification-apiauth)
6. [API Chambres (`/rooms`)](#6-api-chambres-rooms)
7. [API Réservations (`/bookings`)](#7-api-réservations-bookings)
8. [API Statistiques (`/api/stats`)](#8-api-statistiques-apistats)
9. [Scénario de test complet (ordre recommandé)](#9-scénario-de-test-complet-ordre-recommandé)
10. [Scripts Postman utiles (Tests)](#10-scripts-postman-utiles-tests)
11. [Dépannage détaillé](#11-dépannage-détaillé)

---

## 1. Prérequis et démarrage

| Élément | Détail |
|--------|--------|
| **Postman** | Installé depuis [postman.com/downloads](https://www.postman.com/downloads/) |
| **Backend** | Application Spring Boot lancée (IDE ou `mvn spring-boot:run`) |
| **Port** | Par défaut **8080** (voir `application.properties` si vous l’avez changé) |
| **MySQL** | Serveur démarré, base créée selon `spring.datasource.url` (ex. `bd_pfa_hotel`) |
| **URL de base** | `http://localhost:8080` |

**Variable Postman recommandée** : créez une variable `base_url` = `http://localhost:8080` et utilisez `{{base_url}}` dans toutes les URLs.

---

## 2. Configurer Postman (environnement + variables)

### Étapes

1. Cliquez sur **Environments** (icône œil / engrenage) → **Create Environment**.
2. Nom : `Hotel Local`.
3. Ajoutez les variables :

| Variable | Valeur initiale | Usage |
|----------|-----------------|--------|
| `base_url` | `http://localhost:8080` | Préfixe de toutes les URLs |
| `token` | *(vide au début)* | Rempli automatiquement après login (script) ou manuellement |

4. Sélectionnez l’environnement **Hotel Local** en haut à droite.
5. Dans une requête, l’URL devient par exemple : `{{base_url}}/rooms/all-rooms`.

### Bearer Token sur une collection

1. Clic droit sur la collection → **Edit** → **Authorization**.
2. Type : **Bearer Token**.
3. Token : `{{token}}` (après avoir défini `token` dans l’environnement).

**Note** : pour les routes **publiques**, vous pouvez laisser le token vide ou dupliquer les requêtes sans Authorization.

---

## 3. JWT : quand envoyer le token ?

Le backend utilise **Spring Security** + **JWT**. Le header attendu est :

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Qui doit envoyer un JWT ?

- **Routes réservées au rôle `ADMIN`** : ajout / modification / suppression de chambres, liste globale des réservations, réservations par chambre (admin), tableau de bord stats.
- **Routes publiques** (sans JWT) : lecture des chambres (GET `/rooms/**`), création d’une réservation `POST /bookings/room/{id}/booking`, consultation par code, suppression réservation (selon configuration actuelle).

### Compte administrateur par défaut

Si la table `app_users` était **vide** au premier démarrage, un compte a pu être créé automatiquement :

| Champ | Valeur |
|-------|--------|
| **Email** | `admin@hotel.local` |
| **Mot de passe** | `admin123` |
| **Rôle** | `ADMIN` |

Un utilisateur créé via **register** reçoit le rôle **STAFF** : il **ne peut pas** appeler les routes réservées à `ADMIN` (gestion des chambres, stats, etc.).

---

## 4. Tableau récapitulatif des endpoints

| Méthode | Chemin | Body | JWT |
|---------|--------|------|-----|
| POST | `/api/auth/login` | JSON | Non |
| POST | `/api/auth/register` | JSON | Non |
| GET | `/rooms/all-rooms` | — | Non |
| GET | `/rooms/search` | — | Non |
| GET | `/rooms/room-types` | — | Non |
| GET | `/rooms/room/{roomId}` | — | Non |
| GET | `/rooms/room/{roomId}/photo` | — | Non |
| GET | `/rooms/room/{roomId}/availability` | — | Non |
| POST | `/rooms/add/new-room` | **form-data** | **Oui (ADMIN)** |
| PUT | `/rooms/update/room/{roomId}` | **form-data** | **Oui (ADMIN)** |
| DELETE | `/rooms/delete/room/{roomId}` | — | **Oui (ADMIN)** |
| POST | `/bookings/room/{roomId}/booking` | JSON | Non |
| GET | `/bookings/confirmation/{code}` | — | Non |
| GET | `/bookings/all-bookings` | — | **Oui (ADMIN)** |
| GET | `/bookings/room/{roomId}/bookings` | — | **Oui (ADMIN)** |
| DELETE | `/bookings/booking/{bookingId}/delete` | — | Non* |
| GET | `/api/stats/dashboard` | — | **Oui (ADMIN)** |

\*La configuration de sécurité peut évoluer ; vérifiez `SecurityConfig.java` si un 403 apparaît.

---

## 5. API Authentification (`/api/auth`)

### 5.1 POST `/api/auth/login`

**Objectif** : obtenir un **JWT** pour les routes admin.

| Propriété | Valeur |
|-----------|--------|
| **URL complète** | `{{base_url}}/api/auth/login` |
| **Méthode** | `POST` |
| **Headers** | `Content-Type: application/json` (automatique si Body = raw JSON) |
| **Body** | **raw** → **JSON** |
| **Authorization** | Aucune |

**Corps JSON (champs obligatoires)** :

| Champ | Type | Description |
|-------|------|-------------|
| `email` | string | Email enregistré (souvent normalisé en minuscules côté serveur) |
| `password` | string | Mot de passe en clair |

**Exemple de body** :

```json
{
  "email": "admin@hotel.local",
  "password": "admin123"
}
```

**Réponse succès (200)** — structure typique :

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "email": "admin@hotel.local",
  "role": "ADMIN",
  "message": "Connexion réussie."
}
```

**Réponse erreur (400)** : email/mot de passe manquants ou message dans `message`.

---

### 5.2 POST `/api/auth/register`

**Objectif** : créer un compte **STAFF** et recevoir un JWT.

| Propriété | Valeur |
|-----------|--------|
| **URL** | `{{base_url}}/api/auth/register` |
| **Méthode** | `POST` |
| **Body** | **raw** → **JSON** |

**Corps JSON** :

| Champ | Type | Contraintes |
|-------|------|-------------|
| `email` | string | Non vide, unique |
| `password` | string | **Minimum 6 caractères** |

**Exemple** :

```json
{
  "email": "staff@monhotel.com",
  "password": "secret12"
}
```

**Réponse succès (200)** : `token`, `email`, `role` (`STAFF`), `message`.

**Réponse erreur (400)** : email déjà utilisé, ou validation mot de passe / email.

---

## 6. API Chambres (`/rooms`)

### 6.1 GET `/rooms/all-rooms`

**Body** : aucun (ne pas ouvrir l’onglet Body ou laisser vide).

**Réponse** : tableau JSON de chambres avec notamment `id`, `roomType`, `roomPrice`, `photo` (base64), `booked`.

---

### 6.2 GET `/rooms/search`

**Paramètres** (query string, onglet **Params** dans Postman) :

| Paramètre | Obligatoire | Description |
|-----------|-------------|-------------|
| `page` | Non | Numéro de page (0 = première page) |
| `size` | Non | Taille (défaut souvent 8 côté serveur) |
| `sort` | Non | Ex. `id` |
| `roomType` | Non | Filtre exact sur le type |
| `minPrice` | Non | Prix minimum (décimal) |
| `maxPrice` | Non | Prix maximum (décimal) |

**Exemple d’URL** :

```
{{base_url}}/rooms/search?page=0&size=10&roomType=Suite&minPrice=50&maxPrice=300
```

**Réponse** : objet JSON de pagination Spring (`content`, `totalElements`, `totalPages`, etc.).

---

### 6.3 GET `/rooms/room-types`

**Body** : aucun.

**Réponse** : tableau de chaînes de types de chambres distincts.

---

### 6.4 GET `/rooms/room/{roomId}`

**Path variable** : `roomId` → identifiant numérique (ex. `1`).

**URL** : `{{base_url}}/rooms/room/1`

**Réponse** : une chambre (JSON) avec photo en base64.

---

### 6.5 GET `/rooms/room/{roomId}/photo`

**Body** : aucun.

**Réponse** : flux binaire **image/jpeg** (pas du JSON). Dans Postman, vous pouvez **Send and Download** pour enregistrer le fichier.

---

### 6.6 GET `/rooms/room/{roomId}/availability`

**Path** : `roomId`

**Query** :

| Paramètre | Format | Description |
|-----------|--------|-------------|
| `checkIn` | `YYYY-MM-DD` | Date d’arrivée |
| `checkOut` | `YYYY-MM-DD` | Date de départ |

**Exemple** :

```
{{base_url}}/rooms/room/1/availability?checkIn=2026-08-01&checkOut=2026-08-07
```

**Réponse** : JSON avec `available` (boolean) et `reason` (string ou null).

---

### 6.7 POST `/rooms/add/new-room` — **ADMIN + JWT**

| Propriété | Valeur |
|-----------|--------|
| **Authorization** | Bearer Token (voir [section 3](#3-jwt--quand-envoyer-le-token)) |
| **Body** | **form-data** — **pas** de raw JSON |

**Clés form-data** :

| Key | Type dans Postman | Obligatoire | Description |
|-----|-------------------|-------------|-------------|
| `photo` | **File** | Oui | Fichier image (jpg, png, …) |
| `roomType` | Text | Oui | Libellé du type de chambre |
| `roomPrice` | Text | Oui | Prix décimal (ex. `99.99`) |

**Dans Postman** :

1. Onglet **Body** → cocher **form-data**.
2. Ligne `photo` : à droite du champ Key, passer le type de **Text** à **File**, puis **Select Files**.
3. Lignes `roomType` et `roomPrice` en **Text**.

**Réponse succès** : **201 Created** avec `id`, `roomType`, `roomPrice`.

---

### 6.8 PUT `/rooms/update/room/{roomId}` — **ADMIN + JWT**

**Path** : `roomId`

**Body** : **form-data** — tous les champs sont **optionnels** ; envoyez seulement ce que vous voulez modifier.

| Key | Type | Description |
|-----|------|-------------|
| `roomType` | Text | Nouveau type |
| `roomPrice` | Text | Nouveau prix |
| `photo` | File | Nouvelle image |

---

### 6.9 DELETE `/rooms/delete/room/{roomId}` — **ADMIN + JWT**

**Body** : aucun.

**Réponse** : **204 No Content** en cas de succès.

---

## 7. API Réservations (`/bookings`)

### 7.1 POST `/bookings/room/{roomId}/booking`

**Remplacez `{roomId}`** par l’ID d’une chambre existante.

| Propriété | Valeur |
|-----------|--------|
| **URL** | `{{base_url}}/bookings/room/1/booking` |
| **Méthode** | `POST` |
| **Authorization** | Aucune (route publique) |
| **Body** | **raw** → **JSON** |

**Corps JSON — noms exacts des champs (Java `BookingRequest`)** :

| Champ | Type JSON | Description |
|-------|-----------|-------------|
| `checkInDate` | string | Date ISO `YYYY-MM-DD` |
| `checkOutDate` | string | Date ISO `YYYY-MM-DD` (doit être **après** le check-in) |
| `guestFullName` | string | Nom complet |
| `guestEmail` | string | Email |
| `numberOfAdults` | number | Entier ≥ 1 |
| `numberOfChildren` | number | Entier ≥ 0 |

**Exemple complet** :

```json
{
  "checkInDate": "2026-09-10",
  "checkOutDate": "2026-09-15",
  "guestFullName": "Marie Martin",
  "guestEmail": "marie.martin@email.com",
  "numberOfAdults": 2,
  "numberOfChildren": 0
}
```

**Réponse succès (200)** — structure typique :

```json
{
  "confirmationCode": "1234567890",
  "message": "Réservation enregistrée avec succès."
}
```

**Réponse erreur (400)** : corps souvent texte ou message métier (dates invalides, chambre indisponible, etc.).

---

### 7.2 GET `/bookings/confirmation/{confirmationCode}`

**Path** : code de confirmation (chaîne, ex. `1234567890`).

**Body** : aucun.

**Réponse** : détail de la réservation + informations chambre (JSON).

---

### 7.3 GET `/bookings/all-bookings` — **ADMIN + JWT**

**Body** : aucun.

**Réponse** : liste de toutes les réservations.

---

### 7.4 GET `/bookings/room/{roomId}/bookings` — **ADMIN + JWT**

**Path** : `roomId`

**Réponse** : liste des réservations pour cette chambre.

---

### 7.5 DELETE `/bookings/booking/{bookingId}/delete`

**Path** : `bookingId` = identifiant de la réservation (clé primaire `bookingId`).

**Body** : aucun.

**Réponse** : **204 No Content** si succès.

---

## 8. API Statistiques (`/api/stats`)

### GET `/api/stats/dashboard` — **ADMIN + JWT**

**Body** : aucun.

**Réponse** — champs typiques (`HotelDashboardStats`) :

| Champ | Signification |
|-------|----------------|
| `totalRooms` | Nombre total de chambres |
| `totalBookings` | Nombre total de réservations |
| `bookedRoomCount` | Chambres dont le flag « réservé » est vrai |
| `averageRoomPrice` | Prix moyen (décimal) |

---

## 9. Scénario de test complet (ordre recommandé)

1. **POST** `/api/auth/login` avec le compte admin → copier `token` dans `{{token}}` (ou script).
2. **POST** `/rooms/add/new-room` avec **Bearer** + **form-data** → noter `id` retourné.
3. **GET** `/rooms/all-rooms` ou `/rooms/room/{id}`.
4. **GET** `/rooms/room/{id}/availability?checkIn=...&checkOut=...`.
5. **POST** `/bookings/room/{id}/booking` avec **JSON** (sans Bearer).
6. **GET** `/bookings/confirmation/{confirmationCode}`.
7. **GET** `/bookings/all-bookings` avec **Bearer**.
8. **GET** `/api/stats/dashboard` avec **Bearer**.
9. (Optionnel) **DELETE** réservation puis **DELETE** chambre pour nettoyer.

---

## 10. Scripts Postman utiles (Tests)

### Sauvegarder le token après login

Onglet **Tests** de la requête `login` :

```javascript
pm.test("Status 200", function () {
    pm.response.to.have.status(200);
});
const json = pm.response.json();
if (json.token) {
    pm.environment.set("token", json.token);
    console.log("Token enregistré dans l'environnement.");
}
```

### Vérifier une création de chambre

```javascript
pm.test("Created 201", function () {
    pm.response.to.have.status(201);
});
const json = pm.response.json();
pm.environment.set("last_room_id", json.id);
```

---

## 11. Dépannage détaillé

### 401 Unauthorized / 403 Forbidden

- **403** sur POST `/rooms/...` : ajoutez `Authorization: Bearer ...` avec un compte **ADMIN**.
- **403** avec compte **STAFF** : normal — les routes admin exigent **ADMIN**.
- Vérifiez que le token n’est pas expiré (durée dans `application.properties`, clé `jwt.expiration-ms`).

### 400 Bad Request (réservation)

- `checkOutDate` doit être **strictement après** `checkInDate` (règle métier côté serveur).
- Les dates ne doivent pas chevaucher une réservation existante pour la même chambre.
- Vérifiez les **noms** des champs JSON (`numberOfAdults`, pas `numAdults`).

### 415 Unsupported Media Type

- JSON : onglet Body → **raw** → type **JSON** (pas Text).
- Ne pas envoyer du JSON pour `/rooms/add/new-room` : utiliser **form-data**.

### 404 Not Found

- URL ou `roomId` / `bookingId` incorrect.
- `confirmationCode` inexistant.

### 500 Internal Server Error

- Consulter la console du serveur Spring Boot.
- MySQL arrêté ou mauvaise URL / mot de passe dans `application.properties`.
- Image trop grosse : voir `spring.servlet.multipart.max-file-size` et limites MySQL `max_allowed_packet`.

### Erreur MySQL « Packet too large »

Pour les grandes images en BLOB, augmenter temporairement :

```sql
SET GLOBAL max_allowed_packet=10485760;
```

(Perméanence : fichier de configuration MySQL.)

### CORS

Postman **n’applique pas** les mêmes règles que le navigateur ; une erreur CORS sur le site React ne se reproduit pas forcément dans Postman.

---

## Fichiers de référence dans le projet

| Fichier | Rôle |
|---------|------|
| `controller/RoomController.java` | Routes chambres |
| `controller/BookingController.java` | Routes réservations |
| `controller/AuthController.java` | Login / register |
| `controller/HotelStatsController.java` | Stats |
| `security/SecurityConfig.java` | Qui a accès à quoi |
| `dto/BookingRequest.java` | Champs du JSON de réservation |
| `dto/LoginRequest.java` / `RegisterRequest.java` | Champs auth |

---

*Document généré pour le module `projectPfa-hotel`. Mettez à jour ce guide si vous ajoutez des endpoints ou modifiez la sécurité.*
