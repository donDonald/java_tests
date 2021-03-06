package com.idunno.sonisafe.service;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

public class ServerError extends Exception {

    public enum Type {
        UNEXPECTED_SERVER_RESPONSE(0, "UNEXPECTED_SERVER_RESPONSE"),
        EXCEPTION(1, "EXCEPTION"),
        ERROR(2, "ERROR");
        private static Type[] allValues = values();
        public static Type fromOrdinal(int n) {return allValues[n];}
        private final int    value;
        private final String text;
        private Type(int value, String text ) { this.value = value; this.text = text; }
        public int getValue() { return value; }
        public String getText() { return text; }
    }

    public ServerError(Type type, String description) {
        m_type = type;
        m_text = description;
    }

    public ServerError(byte[] array) {
        try {
            String str = new String(array, "UTF-8");
            List<String> list = Arrays.asList(str.split("<<>>"));
            m_type = Type.fromOrdinal( Integer.valueOf(list.get(0)) );
            m_text = list.get(1);
        } catch(Exception e) {
            assert(false);
        }
    }

    public byte[] array() {
        String tmp= Integer.toHexString(m_type.getValue()) + "<<>>" + m_text;
        return tmp.getBytes();
    }

    public String toString() {

        String result = m_type.getText();
        if( m_text != null ) {
            result += ": " + m_text;
        }
        return result;
    }

    private Type      m_type;
    private String    m_text;

}
