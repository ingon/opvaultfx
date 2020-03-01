package dev.ingon.opvaultfx;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

class KeyboardChar {
    protected final int code;
    protected final int modifier;
    
    private KeyboardChar(int code) {
        this(code, KeyEvent.VK_UNDEFINED);
    }
    
    private KeyboardChar(int code, int modifier) {
        this.code = code;
        this.modifier = modifier;
    }

    private static final Map<Character, KeyboardChar> COMBOS = new HashMap<Character, KeyboardChar>();
    static {
        for (char ch = '0'; ch <= '9'; ch++) {
            COMBOS.put(ch, new KeyboardChar(ch));
        }
        for (char ch = 'a'; ch <= 'z'; ch++) {
            COMBOS.put(ch, new KeyboardChar(Character.toUpperCase(ch)));
        }
        for (char ch = 'A'; ch <= 'Z'; ch++) {
            COMBOS.put(ch, new KeyboardChar(ch, KeyEvent.VK_SHIFT));
        }
        String numrow = ")!@#$%^&*(";
        for (int i = 0; i < numrow.length(); i++) {
            COMBOS.put(numrow.charAt(i), new KeyboardChar('0' + i, KeyEvent.VK_SHIFT));
        }
        COMBOS.put('-', new KeyboardChar(KeyEvent.VK_MINUS));
        COMBOS.put('_', new KeyboardChar(KeyEvent.VK_MINUS, KeyEvent.VK_SHIFT));
        
        COMBOS.put('=', new KeyboardChar(KeyEvent.VK_EQUALS));
        COMBOS.put('+', new KeyboardChar(KeyEvent.VK_EQUALS, KeyEvent.VK_SHIFT));

        COMBOS.put('[', new KeyboardChar(KeyEvent.VK_OPEN_BRACKET));
        COMBOS.put('{', new KeyboardChar(KeyEvent.VK_BRACELEFT, KeyEvent.VK_SHIFT));
        
        COMBOS.put(']', new KeyboardChar(KeyEvent.VK_CLOSE_BRACKET));
        COMBOS.put('}', new KeyboardChar(KeyEvent.VK_BRACERIGHT, KeyEvent.VK_SHIFT));
        
        COMBOS.put('\\', new KeyboardChar(KeyEvent.VK_BACK_SLASH));
        COMBOS.put('|', new KeyboardChar(KeyEvent.VK_BACK_SLASH, KeyEvent.VK_SHIFT));
        
        COMBOS.put(';', new KeyboardChar(KeyEvent.VK_SEMICOLON));
        COMBOS.put(':', new KeyboardChar(KeyEvent.VK_SEMICOLON, KeyEvent.VK_SHIFT));

        COMBOS.put('\'', new KeyboardChar(KeyEvent.VK_QUOTE));
        COMBOS.put('"', new KeyboardChar(KeyEvent.VK_QUOTE, KeyEvent.VK_SHIFT));
        
        COMBOS.put(',', new KeyboardChar(KeyEvent.VK_COMMA));
        COMBOS.put('<', new KeyboardChar(KeyEvent.VK_COMMA, KeyEvent.VK_SHIFT));
        
        COMBOS.put('.', new KeyboardChar(KeyEvent.VK_PERIOD));
        COMBOS.put('>', new KeyboardChar(KeyEvent.VK_PERIOD, KeyEvent.VK_SHIFT));

        COMBOS.put('/', new KeyboardChar(KeyEvent.VK_SLASH));
        COMBOS.put('?', new KeyboardChar(KeyEvent.VK_SLASH, KeyEvent.VK_SHIFT));
        
        COMBOS.put('`', new KeyboardChar(KeyEvent.VK_BACK_QUOTE));
        COMBOS.put('~', new KeyboardChar(KeyEvent.VK_BACK_QUOTE, KeyEvent.VK_SHIFT));
        
        COMBOS.put(' ', new KeyboardChar(KeyEvent.VK_SPACE));
    }
    
    public static KeyboardChar get(char ch) {
        return COMBOS.get(ch);
    }
}