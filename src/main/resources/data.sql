-- Insertion des Permissions de base
INSERT INTO permissions (name, description) VALUES ('READ_STOCK', 'Lecture du stock et des produits') ON CONFLICT (name) DO NOTHING;
INSERT INTO permissions (name, description) VALUES ('WRITE_STOCK', 'Ajout et modification de produits/catégories') ON CONFLICT (name) DO NOTHING;
INSERT INTO permissions (name, description) VALUES ('DELETE_STOCK', 'Suppression de produits') ON CONFLICT (name) DO NOTHING;
INSERT INTO permissions (name, description) VALUES ('MANAGE_USERS', 'Gestion des utilisateurs et rôles') ON CONFLICT (name) DO NOTHING;
INSERT INTO permissions (name, description) VALUES ('MANAGE_ORDERS', 'Gestion des commandes') ON CONFLICT (name) DO NOTHING;
INSERT INTO permissions (name, description) VALUES ('MANAGE_CAISSE', 'Gestion des caisses et trésorerie') ON CONFLICT (name) DO NOTHING;

-- Insertion des Rôles principaux
INSERT INTO roles (name, description) VALUES ('ROLE_SUPER_ADMIN', 'Accès total à la plateforme') ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name, description) VALUES ('ROLE_ADMIN', 'Administration et paramétrage') ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name, description) VALUES ('ROLE_MANAGER', 'Gestion opérationnelle stock et commandes') ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name, description) VALUES ('ROLE_EMPLOYEE', 'Opérations quotidiennes et caisse') ON CONFLICT (name) DO NOTHING;
