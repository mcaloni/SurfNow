ALTER TABLE beaches ADD COLUMN windguru_spot_id INT;

-- Spot IDs confirmados em windguru.cz
UPDATE beaches SET windguru_spot_id = 74478 WHERE name = 'Maresias';
UPDATE beaches SET windguru_spot_id = 74458 WHERE name = 'Saquarema';

-- Novas praias
INSERT INTO beaches (name, state, city, latitude, longitude, ideal_wave_min, ideal_wave_max, ideal_wind_direction, best_swell_direction, description, windguru_spot_id)
VALUES
    ('Guarujá',         'SP', 'Guarujá',          -23.9928, -46.2575, 0.8, 2.0, 'N', 'S', 'Ondas de boa qualidade no litoral paulista', 270),
    ('Ilhabela',        'SP', 'Ilhabela',          -23.7778, -45.3583, 0.8, 2.5, 'N', 'S', 'Picos variados em ilha paradisíaca', 322),
    ('Ipanema',         'RJ', 'Rio de Janeiro',    -22.9868, -43.2045, 0.8, 2.0, 'N', 'S', 'Berço do surf carioca', 554795),
    ('Barra da Tijuca', 'RJ', 'Rio de Janeiro',    -23.0000, -43.3650, 0.8, 2.5, 'N', 'S', 'Ondas longas e constantes no Rio', NULL),
    ('Itacoatiara',     'RJ', 'Niterói',           -22.9667, -43.0333, 1.0, 3.0, 'N', 'S', 'Ondas pesadas e desafiadoras', NULL),
    ('Praia do Rosa',   'SC', 'Imbituba',          -28.1333, -48.6667, 0.8, 2.5, 'N', 'S', 'Ondas perfeitas no sul catarinense', NULL),
    ('Torres',          'RS', 'Torres',            -29.3347, -49.7267, 0.8, 2.5, 'N', 'S', 'Surf no extremo sul do Brasil', NULL);
