CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE users (
    id           UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    email        VARCHAR(255) NOT NULL UNIQUE,
    name         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE beaches (
    id                    UUID             PRIMARY KEY DEFAULT uuid_generate_v4(),
    name                  VARCHAR(255)     NOT NULL,
    state                 VARCHAR(2)       NOT NULL,
    city                  VARCHAR(255)     NOT NULL,
    latitude              DOUBLE PRECISION NOT NULL,
    longitude             DOUBLE PRECISION NOT NULL,
    ideal_wave_min        DOUBLE PRECISION NOT NULL,
    ideal_wave_max        DOUBLE PRECISION NOT NULL,
    ideal_wind_direction  VARCHAR(2)       NOT NULL,
    best_swell_direction  VARCHAR(2)       NOT NULL,
    photo_url             VARCHAR(500),
    description           TEXT,
    created_at            TIMESTAMP        NOT NULL DEFAULT NOW()
);

CREATE TABLE surf_reports (
    id          UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    beach_id    UUID        NOT NULL REFERENCES beaches(id),
    user_id     UUID        NOT NULL REFERENCES users(id),
    crowd_level VARCHAR(20) NOT NULL,
    photo_url   VARCHAR(500),
    description VARCHAR(500),
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_surf_reports_beach_id   ON surf_reports(beach_id);
CREATE INDEX idx_surf_reports_created_at ON surf_reports(created_at DESC);
CREATE INDEX idx_beaches_state           ON beaches(state);

-- Seed: praias brasileiras populares
INSERT INTO beaches (name, state, city, latitude, longitude, ideal_wave_min, ideal_wave_max, ideal_wind_direction, best_swell_direction, description)
VALUES
    ('Maresias',   'SP', 'São Sebastião', -23.8167, -45.5583, 0.8, 2.5, 'N', 'S', 'Ondas tubulares, destino preferido dos surfistas paulistas'),
    ('Itamambuca', 'SP', 'Ubatuba',       -23.4297, -45.1158, 1.0, 3.0, 'N', 'S', 'Uma das melhores praias de surf do litoral norte de SP'),
    ('Saquarema',  'RJ', 'Saquarema',     -22.9272, -42.5108, 1.0, 3.5, 'N', 'S', 'Capital do surf brasileiro, sede de etapas do WSL'),
    ('Ferrugem',   'SC', 'Garopaba',      -28.0225, -48.5444, 0.8, 2.5, 'N', 'S', 'Ondas consistentes no sul do Brasil'),
    ('Joaquina',   'SC', 'Florianópolis', -27.6553, -48.4458, 1.0, 3.0, 'N', 'S', 'Palco de campeonatos nacionais e internacionais');
