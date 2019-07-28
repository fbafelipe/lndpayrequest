CREATE TABLE IF NOT EXISTS Invoice (
	id  BIGINT UNSIGNED  NOT NULL   AUTO_INCREMENT,
	paymentId  VARCHAR(255) NOT NULL  UNIQUE,
	accountId  BIGINT UNSIGNED NOT NULL,
	rHash  VARCHAR(255)  NOT NULL,
	paymentRequest  TEXT  NOT NULL,
	amountSat  BIGINT   NOT NULL,
	date  DATETIME NOT NULL,
	paid  BOOLEAN  NOT NULL,
PRIMARY KEY(id),
INDEX Invoice_paymentIdIndex(paymentId),
INDEX Invoice_accountIdIndex(accountId)
);

CREATE TABLE IF NOT EXISTS Account (
	id  BIGINT UNSIGNED  NOT NULL   AUTO_INCREMENT,
	username   VARCHAR(255) NOT NULL  UNIQUE,
	apikey   VARCHAR(255) NOT NULL  UNIQUE,
	passwordHash   VARCHAR(255) NOT NULL,
	passwordSalt   VARCHAR(255) NOT NULL,
PRIMARY KEY(id),
INDEX Account_usernameIndex(username),
INDEX Account_apikeyIndex(apikey)
);

CREATE TABLE IF NOT EXISTS Withdraw (
	id  BIGINT UNSIGNED  NOT NULL   AUTO_INCREMENT,
	accountId  BIGINT UNSIGNED NOT NULL,
	amountSat  BIGINT   NOT NULL,
	date  DATETIME NOT NULL,
PRIMARY KEY(id),
INDEX Withdraw_accountIdIndex(accountId)
);


CREATE TABLE IF NOT EXISTS Quote (
	currency  VARCHAR(255)  NOT NULL  UNIQUE,
	bitcoinValue  DOUBLE   NOT NULL,
	lastUpdate  DATETIME NOT NULL,
PRIMARY KEY(currency)
);
