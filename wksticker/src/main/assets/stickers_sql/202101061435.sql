create table custom_sticker(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    path varchar(100) NOT NULL default '',
    sort_num INTEGER NOT NULL default 0,
    format varchar(40) not null default '',
    placeholder text NOT NULL default '',
    category varchar(100) not null default '',
    width INTEGER NOT NULL default 0,
    height INTEGER NOT NULL default 0
);

create table user_sticker_category(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    sort_num INTEGER NOT NULL default 0,
    category varchar(100) not null default '',
    cover varchar(100) not null default ''
);

create table sticker(
   id INTEGER PRIMARY KEY AUTOINCREMENT,
   category varchar(100) not null default '',
   path varchar(100) NOT NULL default '',
   width INTEGER NOT NULL default 0,
   height INTEGER NOT NULL default 0,
   title varchar(100) NOT NULL default '',
   placeholder text NOT NULL default '',
   format text not null default '',
   searchable_word varchar(100) NOT NULL default ''
);