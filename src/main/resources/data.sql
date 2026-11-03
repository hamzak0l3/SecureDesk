-- ==============================================================================
-- SecureDesk - Données initiales
-- Insertion idempotente (ON CONFLICT DO NOTHING) compatible PostgreSQL
-- ==============================================================================

-- ---- Catégories de tickets ----
INSERT INTO categories (id, name, description, active)
VALUES (1, 'Réseau', 'Problèmes liés au réseau et à la connectivité', true)
ON CONFLICT (id) DO NOTHING;

INSERT INTO categories (id, name, description, active)
VALUES (2, 'Logiciel', 'Problèmes liés aux logiciels et applications', true)
ON CONFLICT (id) DO NOTHING;

INSERT INTO categories (id, name, description, active)
VALUES (3, 'Matériel', 'Problèmes liés au matériel informatique', true)
ON CONFLICT (id) DO NOTHING;

-- ---- Utilisateurs ----
-- Mots de passe hachés avec BCrypt (coût 10)
--   admin123  -> $2a$10$WGq2P9eamMOkJvBGfSh5JeXHRx.2TSKnzB0PKVFQDhz0J7yBuyMuS
--   agent123  -> $2a$10$ByIRiMGSsmKjpFn4PjXnl.UzNEcy3MnMY6UJfmBEkAAZgBR7P6K9K
--   client123 -> $2a$10$J7aVVMI0sy/M3q8U0UZHhuHolDw0/XNg1Vi0hUm6C43IK3bsdRZ66

-- Administrateur
INSERT INTO users (id, username, password, email, full_name, role, active, created_at, updated_at)
VALUES (1, 'admin', '$2a$10$WGq2P9eamMOkJvBGfSh5JeXHRx.2TSKnzB0PKVFQDhz0J7yBuyMuS', 'admin@securedesk.fr', 'Administrateur Système', 'ROLE_ADMIN', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- Agent de support
INSERT INTO users (id, username, password, email, full_name, role, active, created_at, updated_at)
VALUES (2, 'agent', '$2a$10$ByIRiMGSsmKjpFn4PjXnl.UzNEcy3MnMY6UJfmBEkAAZgBR7P6K9K', 'agent@securedesk.fr', 'Agent Support', 'ROLE_AGENT', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- Client
INSERT INTO users (id, username, password, email, full_name, role, active, created_at, updated_at)
VALUES (3, 'client', '$2a$10$J7aVVMI0sy/M3q8U0UZHhuHolDw0/XNg1Vi0hUm6C43IK3bsdRZ66', 'client@securedesk.fr', 'Client Test', 'ROLE_CLIENT', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- Second client (démonstration IDOR V3)
INSERT INTO users (id, username, password, email, full_name, role, active, created_at, updated_at)
VALUES (4, 'client1', '$2a$10$J7aVVMI0sy/M3q8U0UZHhuHolDw0/XNg1Vi0hUm6C43IK3bsdRZ66', 'client1@securedesk.fr', 'Client Test 1', 'ROLE_CLIENT', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- Mise à jour des séquences pour éviter les conflits d'ID
SELECT setval(pg_get_serial_sequence('categories', 'id'), (SELECT MAX(id) FROM categories));
SELECT setval(pg_get_serial_sequence('users', 'id'), (SELECT MAX(id) FROM users));
