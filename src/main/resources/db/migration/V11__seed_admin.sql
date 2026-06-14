-- Seed a single super-admin (Gabe Sechan) and their password so the admin app
-- can be exercised end to end. The hash is a pre-computed BCrypt(16) value.

INSERT INTO public.admins (id, email, name, phone)
VALUES ('a0000000-0000-0000-0000-000000000001', 'gsechan@hotmail.com', 'Gabe Sechan', '2067140469');

INSERT INTO public.adminpasswords (id, hash, admin_id)
VALUES (
    'a0000000-0000-0000-0000-000000000002',
    '$2a$16$yVOk87qUPSZ..TymJFFyuut73/VygxxOYcquIpe52/00qmlLxbosy',
    'a0000000-0000-0000-0000-000000000001'
);
