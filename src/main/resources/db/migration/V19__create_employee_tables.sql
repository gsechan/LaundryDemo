CREATE TABLE public.employees (
    id uuid NOT NULL PRIMARY KEY,
    name character varying(255) NOT NULL,
    email character varying(255) NOT NULL,
    phone character varying(255) NOT NULL,
    organization_id uuid NOT NULL REFERENCES public.organizations(id)
);

CREATE INDEX idx_employees_organization_id ON public.employees (organization_id);
CREATE INDEX idx_employees_phone ON public.employees (phone);

CREATE TABLE public.employeepasswords (
    id uuid NOT NULL PRIMARY KEY,
    hash character varying(255) NOT NULL,
    employee_id uuid NOT NULL UNIQUE REFERENCES public.employees(id)
);

CREATE INDEX idx_employeepasswords_employee_id ON public.employeepasswords (employee_id);

CREATE TABLE public.employeesessions (
    id uuid NOT NULL PRIMARY KEY,
    token character varying(255) NOT NULL UNIQUE,
    expiration timestamp(6) without time zone NOT NULL,
    employee_id uuid NOT NULL REFERENCES public.employees(id)
);
