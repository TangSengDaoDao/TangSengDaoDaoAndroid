package com.chat.file;


import com.xinbida.wukongim.entity.WKMsg;

/**
 * 2020-08-13 16:39
 */
public class ChooseFileEntity {
    boolean checked;
    WKMsg msg;

    ChooseFileEntity(WKMsg msg) {
        this.msg = msg;
    }
}
