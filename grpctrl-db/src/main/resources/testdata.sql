
-- CREATE DATABASE grpctrl;
-- CREATE USER grpctrl WITH PASSWORD 'password';
-- ALTER ROLE grpctrl WITH CREATEDB;


INSERT INTO accounts (account_id, name) VALUES
(1, 'account-1'),
(2, 'account-2'),
(3, 'account-3'),
(4, 'account-4');


INSERT INTO api_logins (account_id, key, secret) VALUES
(1, 'aaaaaaaaaaaaaaaaaaaa', 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa'),
(2, 'bbbbbbbbbbbbbbbbbbbb', 'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb'),
(3, 'cccccccccccccccccccc', 'cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc'),
(4, 'dddddddddddddddddddd', 'dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd');


INSERT INTO service_levels (account_id, max_groups, max_tags, max_depth) VALUES
(1, 100, 1000, 3),
(2, 100, 1000, 3),
(3, 100, 1000, 3),
(4, 100, 1000, 3);


INSERT INTO users (user_id, login, source) VALUES
(1, 'mtday', 'GITHUB'),
(2, 'test', 'LOCAL');


INSERT INTO user_emails (user_id, email, is_primary, is_verified) VALUES
(1, 'fake@fake.com', true, true),
(2, 'test@test.com', true, true);


INSERT INTO  user_roles (user_id, role) VALUES
(1, 'ADMIN'),
(1, 'USER'),
(2, 'USER');


INSERT INTO user_accounts (user_id, account_id) VALUES
(1, 1),
(1, 2),
(2, 3);


-- Building this groups structure:
--   A
--      A.1
--         A.1.1
--            A.1.1.1
--            A.1.1.2
--            M
--               M.1
--               M.2
--               M.3
--         A.1.2
--         M
--      A.2
--      M
--   B
--      B.1
--      B.2
--      M


-- Two top-level groups A and B.
INSERT INTO groups (group_id, account_id, parent_id, group_name) VALUES
(1,  1, null, 'A'),
(2,  1, null, 'B'),

-- Add three groups as children to A called A.1, A.2, and M
(3,  1,  1, 'A.1'),
(4,  1,  1, 'A.2'),
(5,  1,  1, 'M'),

-- Add three groups as children to B called B.1, B.2, and M
(6,  1,  2, 'B.1'),
(7,  1,  2, 'B.2'),
(8,  1,  2, 'M'),

-- Add three groups as children to A.1 called A.1.1, A.1.2, and M
(9,  1,  3, 'A.1.1'),
(10, 1,  3, 'A.1.2'),
(11, 1,  3, 'M'),

-- Add three groups as children to A.1.1 called A.1.1.1, A.1.1.2, and M
(12, 1,  9, 'A.1.1.1'),
(13, 1,  9, 'A.1.1.2'),
(14, 1,  9, 'M'),

-- Add three groups as children to M (with parent A.1.1) called M.1, M.2, and M.3
(15, 1, 14, 'M.1'),
(16, 1, 14, 'M.2'),
(17, 1, 14, 'M.3');


-- Build the same structure for account-2.
-- Two top-level groups A and B.
INSERT INTO groups (group_id, account_id, parent_id, group_name) VALUES
(18, 2, null, 'A'),
(19, 2, null, 'B'),

-- Add three groups as children to A called A.1, A.2, and M
(20, 2, 18, 'A.1'),
(21, 2, 18, 'A.2'),
(22, 2, 18, 'M'),

-- Add three groups as children to B called B.1, B.2, and M
(23, 2, 19, 'B.1'),
(24, 2, 19, 'B.2'),
(25, 2, 19, 'M'),

-- Add three groups as children to A.1 called A.1.1, A.1.2, and M
(26, 2, 20, 'A.1.1'),
(27, 2, 20, 'A.1.2'),
(28, 2, 20, 'M'),

-- Add three groups as children to A.1.1 called A.1.1.1, A.1.1.2, and M
(29, 2, 26, 'A.1.1.1'),
(30, 2, 26, 'A.1.1.2'),
(31, 2, 26, 'M'),

-- Add three groups as children to M (with parent A.1.1) called M.1, M.2, and M.3
(32, 2, 31, 'M.1'),
(33, 2, 31, 'M.2'),
(34, 2, 31, 'M.3');


INSERT INTO tags (account_id, group_id, tag_label, tag_value) VALUES
(1, 1,  'path', 'A'),
(1, 2,  'path', 'B'),
(1, 3,  'path', 'A  A.1'),
(1, 4,  'path', 'A  A.2'),
(1, 5,  'path', 'A  M'),
(1, 6,  'path', 'B  B.1'),
(1, 7,  'path', 'B  B.2'),
(1, 8,  'path', 'B  M'),
(1, 9,  'path', 'A  A.1  A.1.1'),
(1, 10, 'path', 'A  A.1  A.1.2'),
(1, 11, 'path', 'A  A.1  M'),
(1, 12, 'path', 'A  A.1  A.1.1  A.1.1.1'),
(1, 13, 'path', 'A  A.1  A.1.1  A.1.1.2'),
(1, 14, 'path', 'A  A.1  A.1.1  M'),
(1, 15, 'path', 'A  A.1  A.1.1  M  M.1'),
(1, 16, 'path', 'A  A.1  A.1.1  M  M.2'),
(1, 17, 'path', 'A  A.1  A.1.1  M  M.3'),
(2, 18, 'path', 'A'),
(2, 19, 'path', 'B'),
(2, 20, 'path', 'A  A.1'),
(2, 21, 'path', 'A  A.2'),
(2, 22, 'path', 'A  M'),
(2, 23, 'path', 'B  B.1'),
(2, 24, 'path', 'B  B.2'),
(2, 25, 'path', 'B  M'),
(2, 26, 'path', 'A  A.1  A.1.1'),
(2, 27, 'path', 'A  A.1  A.1.2'),
(2, 28, 'path', 'A  A.1  M'),
(2, 29, 'path', 'A  A.1  A.1.1  A.1.1.1'),
(2, 30, 'path', 'A  A.1  A.1.1  A.1.1.2'),
(2, 31, 'path', 'A  A.1  A.1.1  M'),
(2, 32, 'path', 'A  A.1  A.1.1  M  M.1'),
(2, 33, 'path', 'A  A.1  A.1.1  M  M.2'),
(2, 34, 'path', 'A  A.1  A.1.1  M  M.3');


