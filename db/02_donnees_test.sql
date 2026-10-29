-- =====================================================================
-- Exercice 2 — Service SOAP de clients
-- Jeu de données fictives : 20 clients (identifiants 1 à 20)
--
-- Exécution (après 01_schema.sql) :  mysql -u root -p < db/02_donnees_test.sql
-- Le flux NiFi (nifi/data/input/clients_a_charger.csv) charge ensuite un
-- second lot (identifiants 21+) afin de démontrer la chaîne ETL complète.
-- =====================================================================

USE clientsdb;

INSERT INTO clients (id, nom, prenom, email, telephone, ville, pays, statut) VALUES
  ( 1, 'Ndiaye',      'Amadou',     'amadou.ndiaye@example.com',    '+221 77 101 01 01', 'Dakar',       'Sénégal',      'ACTIF'),
  ( 2, 'Diallo',      'Fatou',      'fatou.diallo@example.com',     '+221 78 202 02 02', 'Thiès',       'Sénégal',      'ACTIF'),
  ( 3, 'Traoré',      'Ibrahim',    'ibrahim.traore@example.com',   '+223 66 303 03 03', 'Bamako',      'Mali',         'ACTIF'),
  ( 4, 'Kouassi',     'Awa',        'awa.kouassi@example.com',      '+225 07 404 04 04', 'Abidjan',     'Côte d’Ivoire','ACTIF'),
  ( 5, 'El Amrani',   'Youssef',    'youssef.elamrani@example.com', '+212 66 150 50 50', 'Casablanca',  'Maroc',        'ACTIF'),
  ( 6, 'Benali',      'Salma',      'salma.benali@example.com',     '+212 66 260 60 60', 'Rabat',       'Maroc',        'INACTIF'),
  ( 7, 'Sow',         'Mariama',    'mariama.sow@example.com',      '+221 76 505 05 05', 'Saint-Louis', 'Sénégal',      'ACTIF'),
  ( 8, 'Camara',      'Ousmane',    'ousmane.camara@example.com',   '+224 62 606 06 06', 'Conakry',     'Guinée',       'ACTIF'),
  ( 9, 'Mensah',      'Kofi',       'kofi.mensah@example.com',      '+233 24 707 07 07', 'Accra',       'Ghana',        'INACTIF'),
  (10, 'Owona',       'Chantal',    'chantal.owona@example.com',    '+237 69 808 08 08', 'Douala',      'Cameroun',     'ACTIF'),
  (11, 'Mbala',       'Jean-Pierre','jeanpierre.mbala@example.com', '+242 06 909 09 09', 'Brazzaville', 'Congo',        'ACTIF'),
  (12, 'Kabongo',     'Grace',      'grace.kabongo@example.com',    '+243 99 110 10 10', 'Kinshasa',    'RD Congo',     'ACTIF'),
  (13, 'Dupont',      'Marie',      'marie.dupont@example.com',     '+33 6 12 34 56 78', 'Paris',       'France',       'ACTIF'),
  (14, 'Martin',      'Julien',     'julien.martin@example.com',    '+33 6 23 45 67 89', 'Lyon',        'France',       'INACTIF'),
  (15, 'Bernard',     'Sophie',     'sophie.bernard@example.com',   '+33 6 34 56 78 90', 'Marseille',   'France',       'ACTIF'),
  (16, 'Fall',        'Cheikh',     'cheikh.fall@example.com',      '+221 77 313 13 13', 'Dakar',       'Sénégal',      'ACTIF'),
  (17, 'Cissé',       'Aminata',    'aminata.cisse@example.com',    '+226 70 414 14 14', 'Ouagadougou', 'Burkina Faso', 'ACTIF'),
  (18, 'Zongo',       'Moussa',     'moussa.zongo@example.com',     '+226 71 515 15 15', 'Bobo-Dioulasso','Burkina Faso','INACTIF'),
  (19, 'Lopez',       'Claire',     'claire.lopez@example.com',     '+33 6 45 67 89 01', 'Toulouse',    'France',       'ACTIF'),
  (20, 'Faye',        'Moussa',     'moussa.faye@example.com',      '+221 78 616 16 16', 'Ziguinchor',  'Sénégal',      'ACTIF');
