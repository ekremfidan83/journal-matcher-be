-- pgvector eklentisini ekle
CREATE EXTENSION IF NOT EXISTS vector;

-- Dergi profilleri tablosu
CREATE TABLE journal_profiles (
                                  id BIGSERIAL PRIMARY KEY,
                                  journal_name VARCHAR(255) NOT NULL,
                                  issn VARCHAR(20) UNIQUE,
                                  vector vector(384),
                                  last_updated TIMESTAMP,
                                  impact_factor DOUBLE PRECISION,
                                  scope TEXT
);

-- Dergi makaleleri tablosu
CREATE TABLE journal_articles (
                                  id BIGSERIAL PRIMARY KEY,
                                  journal_id BIGINT REFERENCES journal_profiles(id),
                                  title TEXT,
                                  article_abstract TEXT,
                                  vector vector(384),
                                  published_date TIMESTAMP,
                                  pmid VARCHAR(20)
);

-- Vektör araması için index
CREATE INDEX ON journal_articles USING ivfflat (vector vector_cosine_ops);