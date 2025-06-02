create table moment_msg(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    action varchar(40) NOT NULL default '',
    action_at INTEGER NOT NULL default 0,
    moment_no varchar(40) NOT NULL default '',
    content text NOT NULL default '',
    uid varchar(40) NOT NULL default '',
    name varchar(100) NOT NULL default '',
    comment text NOT NULL default '',
    version bigint NOT NULL default 0,
    is_deleted int NOT NULL default 0,
    created_at text,
    updated_at text
);