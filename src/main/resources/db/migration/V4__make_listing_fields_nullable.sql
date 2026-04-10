-- Senin Entity'de nullable=true yaptığın tüm alanların NOT NULL kısıtlamasını DB'den kaldırıyoruz
ALTER TABLE listings ALTER COLUMN city DROP NOT NULL;
ALTER TABLE listings ALTER COLUMN address DROP NOT NULL;
ALTER TABLE listings ALTER COLUMN postal_code DROP NOT NULL;
ALTER TABLE listings ALTER COLUMN latitude DROP NOT NULL;
ALTER TABLE listings ALTER COLUMN longitude DROP NOT NULL;
ALTER TABLE listings ALTER COLUMN rent_amount DROP NOT NULL;
ALTER TABLE listings ALTER COLUMN currency DROP NOT NULL;
ALTER TABLE listings ALTER COLUMN available_from DROP NOT NULL;
ALTER TABLE listings ALTER COLUMN furnished DROP NOT NULL;
ALTER TABLE listings ALTER COLUMN utilities_included DROP NOT NULL;
ALTER TABLE listings ALTER COLUMN pets_allowed DROP NOT NULL;
ALTER TABLE listings ALTER COLUMN smoking_allowed DROP NOT NULL;