CREATE TABLE investor
(
    last_name          varchar,
    first_name         varchar,
    date_of_birth      date,
    email              text PRIMARY KEY,
    annual_income      integer,
    phone_number       varchar(10),
    marital_status     varchar,
    number_of_children varchar
);

CREATE TABLE location
(
    country            varchar,
    country_percentage float,
    scpi_id            integer,
    PRIMARY KEY (country, scpi_id)
);

CREATE TABLE sector
(
    name              varchar,
    sector_percentage float,
    scpi_id           integer,
    PRIMARY KEY (name, scpi_id)
);

CREATE TABLE scpi
(
    id                   SERIAL PRIMARY KEY,
    name                 varchar,
    minimum_subscription integer,
    manager              varchar,
    capitalization       BIGINT,
    subscription_fees    float,
    management_costs     float,
    enjoyment_delay      integer,
    iban                 varchar UNIQUE,
    bic                  varchar,
    scheduled_payment    bool,
    cashback             float,
    advertising          text,
    frequency_payment    varchar
);

CREATE TABLE stat_year
(
    year_stat            integer,
    distribution_rate    float,
    share_price          float,
    reconstitution_value float,
    scpi_id              integer,
    PRIMARY KEY (year_stat, scpi_id)
);

ALTER TABLE location
    ADD FOREIGN KEY (scpi_id) REFERENCES scpi (id) ON DELETE CASCADE;

ALTER TABLE sector
    ADD FOREIGN KEY (scpi_id) REFERENCES scpi (id) ON DELETE CASCADE;

ALTER TABLE stat_year
    ADD FOREIGN KEY (scpi_id) REFERENCES scpi (id) ON DELETE CASCADE;

ALTER TABLE scpi ALTER COLUMN subscription_fees TYPE NUMERIC(10,2);
ALTER TABLE scpi ALTER COLUMN management_costs TYPE NUMERIC(10,2);
ALTER TABLE location ALTER COLUMN country_percentage TYPE NUMERIC(10,2);
ALTER TABLE sector ALTER COLUMN sector_percentage TYPE NUMERIC(10,2);
ALTER TABLE stat_year ALTER COLUMN distribution_rate TYPE NUMERIC(10,2);
ALTER TABLE stat_year ALTER COLUMN share_price TYPE NUMERIC(10,2);
ALTER TABLE stat_year ALTER COLUMN reconstitution_value TYPE NUMERIC(10,2);