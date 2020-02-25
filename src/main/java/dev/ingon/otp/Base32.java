package dev.ingon.otp;

import java.nio.ByteBuffer;

public class Base32 {
    public static byte[] decode(String input) {
        int groups = input.length() / 8 + ((input.length() % 8 != 0) ? 1 : 0);
        ByteBuffer buffer = ByteBuffer.allocate(groups * 5);
        for (int begin = 0, n = input.length(); begin < n; begin += 8) {
            int end = Math.min(begin + 8, n);
            byte[] group = decodeGroup(input.substring(begin, end));
            buffer.put(group);
        }
        return buffer.array();
    }
    
    private static byte[] decodeGroup(String input) {
        byte[] result = new byte[5];
        
        var d0 = decode(input.charAt(0));  // [...00000] -> [00000...][........][........][........][........]
        result[0] |= (byte) (d0 << 3); 
        
        var d1 = decode(input.charAt(1));  // [...00000] -> [.....000][00......][........][........][........]
        result[0] |= (byte) (d1 >> 2); 
        result[1] |= (byte) (d1 << 6);
        
        if (input.length() <= 2) {
            byte[] partial = new byte[1];
            System.arraycopy(result, 0, partial, 0, 1);
            return result;
        }
        
        var d2 = decode(input.charAt(2));  // [...00000] -> [.......][..00000.][........][........][........]
        result[1] |= (byte) (d2 << 1);
        
        var d3 = decode(input.charAt(3));  // [...00000] -> [.......][.......0][0000....][........][........]
        result[1] |= (byte) (d3 >> 4);
        result[2] |= (byte) (d3 << 4);
        
        if (input.length() <= 4) {
            byte[] partial = new byte[2];
            System.arraycopy(result, 0, partial, 0, 2);
            return result;
        }
        
        var d4 = decode(input.charAt(4));  // [...00000] -> [.......][........][....0000][0.......][........]
        result[2] |= (byte) (d4 >> 1);
        result[3] |= (byte) (d4 << 7);
        
        if (input.length() <= 5) {
            byte[] partial = new byte[3];
            System.arraycopy(result, 0, partial, 0, 3);
            return result;
        }
        
        var d5 = decode(input.charAt(5));  // [...00000] -> [.......][........][........][.00000..][........]
        result[3] |= (byte) (d5 << 2);
        
        var d6 = decode(input.charAt(6));  // [...00000] -> [.......][........][........][......00][000.....]
        result[3] |= (byte) (d6 >> 3);
        result[4] |= (byte) (d6 << 5);
        
        if (input.length() <= 7) {
            byte[] partial = new byte[4];
            System.arraycopy(result, 0, partial, 0, 4);
            return result;
        }
        
        var d7 = decode(input.charAt(7));  // [...00000] -> [.......][........][........][........][...00000]
        result[4] |= (byte) d7;
                
        return result;
    }

    private static byte decode(char ch) {
        if (ch >= 'A' && ch <= 'Z') {
            return (byte) (ch - 'A');
        } else if (ch >= '2' && ch <= '7') {
            return (byte) (ch - '2' + 26);
        } else if (ch >= 'a' && ch <= 'z') {
            return (byte) (ch - 'a');
        }
        throw new IllegalArgumentException("unknown base32 char");
    }
}
