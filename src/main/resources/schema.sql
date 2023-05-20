CREATE TABLE IF NOT EXISTS users (
  id BIGINT NOT NULL GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  email varchar(255) NOT NULL UNIQUE,
  name varchar(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS requests (
  id BIGINT NOT NULL GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  description varchar(255) NOT NULL,
  requester_id bigint NOT NULL REFERENCES users (id),
  created timestamp without time zone NOT NULL
);

CREATE TABLE IF NOT EXISTS items (
  id BIGINT NOT NULL GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  name varchar(255) NOT NULL,
  description varchar(1000) NOT NULL,
  is_available boolean NOT NULL DEFAULT true,
  owner_id bigint NOT NULL REFERENCES users (id),
  request_id bigint REFERENCES requests (id),
  UNIQUE(id, owner_id)
);

CREATE TABLE IF NOT EXISTS bookings (
  id BIGINT NOT NULL GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  start_date timestamp without time zone NOT NULL,
  end_date timestamp without time zone NOT NULL,
  item_id bigint NOT NULL REFERENCES items (id),
  booker_id bigint NOT NULL REFERENCES users (id),
  status varchar(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS comments (
  id BIGINT NOT NULL GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  text varchar(1000) NOT NULL,
  item_id bigint NOT NULL REFERENCES items (id),
  author_id bigint NOT NULL REFERENCES users (id),
  created timestamp without time zone NOT NULL
);
