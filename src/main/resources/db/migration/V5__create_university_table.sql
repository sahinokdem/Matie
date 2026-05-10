CREATE TABLE universities (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL UNIQUE,
    short_name VARCHAR(50) NOT NULL UNIQUE,
    domain VARCHAR(255) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER trg_universities_updated_at BEFORE UPDATE ON universities FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

INSERT INTO universities (name, short_name, domain)
VALUES
    ('İzmir Yüksek Teknoloji Enstitüsü', 'IYTE', 'iyte.edu.tr'),
    ('Ege Üniversitesi', 'EGE', 'ege.edu.tr'),
    ('Dokuz Eylül Üniversitesi', 'DEU', 'deu.edu.tr'),
    ('İzmir Ekonomi Üniversitesi', 'IEU', 'ieu.edu.tr'),
    ('Yaşar Üniversitesi', 'YASAR', 'yasar.edu.tr');

ALTER TABLE users ADD COLUMN university_id UUID;

UPDATE users
SET university_id = (SELECT id FROM universities WHERE short_name = 'IYTE' LIMIT 1)
WHERE university_id IS NULL;

ALTER TABLE users ALTER COLUMN university_id SET NOT NULL;

ALTER TABLE users
    ADD CONSTRAINT fk_users_university FOREIGN KEY (university_id) REFERENCES universities(id);

CREATE INDEX idx_users_university_id ON users(university_id);
CREATE INDEX idx_universities_active_name ON universities(active, name);
