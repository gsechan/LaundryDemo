CREATE TABLE public.passwords (
    id uuid NOT NULL PRIMARY KEY,
    hash character varying(255) NOT NULL,
    user_id uuid NOT NULL REFERENCES public.users(id)
);

CREATE INDEX idx_password_user_id ON public.passwords (user_id);

CREATE TABLE public.sessions (
    id uuid NOT NULL PRIMARY KEY,
    expiration timestamp(6) without time zone NOT NULL,
    token character varying(255) NOT NULL UNIQUE,
    user_id uuid NOT NULL REFERENCES public.users(id)
);

