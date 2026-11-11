package com.anubhav.codereverse.utils.socket;

import com.anubhav.codereverse.utils.socket.common.ObjectWriter;

public interface OnReadListener {
    void onReadData(String key, Object data, ObjectWriter writer);
}