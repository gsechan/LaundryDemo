CREATE TABLE public.organizations (
    id uuid NOT NULL PRIMARY KEY,
    name character varying(255) NOT NULL UNIQUE,
    default_locale character varying(10)
);

