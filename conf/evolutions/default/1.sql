# --- !Ups

CREATE SEQUENCE player_id_seq;

create table player (
  id bigint default nextval('player_id_seq') primary key,
  name varchar(255) not null
);


# --- !Downs

DROP TABLE player;
DROP SEQUENCE player_id_seq;
