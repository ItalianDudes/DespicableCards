-- TABLE: Key Parameters
CREATE TABLE IF NOT EXISTS key_parameters (
    param_key VARCHAR(32) NOT NULL PRIMARY KEY,
    param_value TEXT
);

-- TABLE: White Cards
CREATE TABLE IF NOT EXISTS whitecards (
    id INTEGER NOT NULL PRIMARY KEY,
    uuid TEXT NOT NULL UNIQUE,
    content TEXT NOT NULL,
    is_wildcard INTEGER NOT NULL DEFAULT 0
);

-- TABLE: Black Cards
CREATE TABLE IF NOT EXISTS blackcards (
    id INTEGER NOT NULL PRIMARY KEY,
    uuid TEXT NOT NULL UNIQUE,
    content TEXT NOT NULL,
    blanks INTEGER NOT NULL
);