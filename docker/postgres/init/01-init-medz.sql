\set ON_ERROR_STOP on

CREATE ROLE medz_loader_owner NOLOGIN;
CREATE ROLE medz_loader_writer LOGIN PASSWORD 'medz_loader_writer';
CREATE ROLE medz_api_reader LOGIN PASSWORD 'medz_api_reader';

GRANT medz_loader_owner TO postgres;

ALTER DATABASE medz OWNER TO medz_loader_owner;

GRANT CONNECT ON DATABASE medz TO medz_loader_writer;
GRANT CONNECT ON DATABASE medz TO medz_api_reader;

GRANT USAGE, CREATE ON SCHEMA public TO medz_loader_writer;
GRANT USAGE ON SCHEMA public TO medz_api_reader;

SET ROLE medz_loader_owner;
\i /medz-loader/sql/ddl.sql
RESET ROLE;

GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO medz_loader_writer;
GRANT USAGE, SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA public TO medz_loader_writer;

GRANT SELECT ON ALL TABLES IN SCHEMA public TO medz_api_reader;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO medz_api_reader;

ALTER DEFAULT PRIVILEGES FOR ROLE medz_loader_owner IN SCHEMA public
  GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO medz_loader_writer;

ALTER DEFAULT PRIVILEGES FOR ROLE medz_loader_owner IN SCHEMA public
  GRANT USAGE, SELECT, UPDATE ON SEQUENCES TO medz_loader_writer;

ALTER DEFAULT PRIVILEGES FOR ROLE medz_loader_owner IN SCHEMA public
  GRANT SELECT ON TABLES TO medz_api_reader;

ALTER DEFAULT PRIVILEGES FOR ROLE medz_loader_owner IN SCHEMA public
  GRANT USAGE, SELECT ON SEQUENCES TO medz_api_reader;
