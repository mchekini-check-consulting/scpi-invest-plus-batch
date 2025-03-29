CREATE TABLE investor (
    last_name          VARCHAR,
    first_name         VARCHAR,
    date_of_birth      DATE,
    email              TEXT PRIMARY KEY,
    annual_income      INTEGER,
    phone_number       VARCHAR(10),
    marital_status     VARCHAR,
    number_of_children VARCHAR
);

CREATE TABLE scpi (
    id                   SERIAL PRIMARY KEY,
    name                 VARCHAR,
    minimum_subscription INTEGER,
    manager              VARCHAR,
    capitalization       BIGINT,
    subscription_fees    NUMERIC(10,2),
    management_costs     NUMERIC(10,2),
    enjoyment_delay      INTEGER,
    iban                 VARCHAR UNIQUE,
    bic                  VARCHAR,
    scheduled_payment    BOOLEAN,
    cashback             FLOAT,
    advertising          TEXT,
    frequency_payment    VARCHAR
);

CREATE TABLE location (
    country            VARCHAR,
    country_percentage NUMERIC(10,2),
    scpi_id            INTEGER,
    PRIMARY KEY (country, scpi_id),
    FOREIGN KEY (scpi_id) REFERENCES scpi (id) ON DELETE CASCADE
);

CREATE TABLE sector (
    name              VARCHAR,
    sector_percentage NUMERIC(10,2),
    scpi_id           INTEGER,
    PRIMARY KEY (name, scpi_id),
    FOREIGN KEY (scpi_id) REFERENCES scpi (id) ON DELETE CASCADE
);

CREATE TABLE stat_year (
    year_stat            INTEGER,
    distribution_rate    NUMERIC(10,2),
    share_price          NUMERIC(10,2),
    reconstitution_value NUMERIC(10,2),
    scpi_id              INTEGER,
    PRIMARY KEY (year_stat, scpi_id),
    FOREIGN KEY (scpi_id) REFERENCES scpi (id) ON DELETE CASCADE
);
