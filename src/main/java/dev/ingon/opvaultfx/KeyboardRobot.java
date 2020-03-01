package dev.ingon.opvaultfx;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;

import dev.ingon.opvault.SecureString;

public class KeyboardRobot {
    private final Robot robot;
    
    public KeyboardRobot() throws AWTException {
        robot = new Robot();
        robot.setAutoWaitForIdle(true);
    }
    
    public void focusPreviousApp() {
        robot.keyPress(KeyEvent.VK_ALT);
        focusNextField();
        robot.keyRelease(KeyEvent.VK_ALT);
    }
    
    public void focusNextField() {
        robot.keyPress(KeyEvent.VK_TAB);
        robot.delay(50);
        robot.keyRelease(KeyEvent.VK_TAB);
    }
    
    public void submit() {
        robot.keyPress(KeyEvent.VK_ENTER);
        robot.delay(50);
        robot.keyRelease(KeyEvent.VK_ENTER);
    }
    
    public void type(String str) {
        robot.delay(50);
        for (int i = 0, n = str.length(); i < n; i++) {
            type(KeyboardChar.get(str.charAt(i)));
        }
    }
    
    public void type(SecureString str) {
        robot.delay(50);
        str.accept((chs) -> {
            for (int i = 0, n = chs.length; i < n; i++) {
                type(KeyboardChar.get(chs[i]));
            }
        });
    }

    private void type(KeyboardChar kc) {
        if (kc.modifier != KeyEvent.VK_UNDEFINED) {
            robot.keyPress(kc.modifier);
        }
        robot.keyPress(kc.code);
        robot.delay(50);
        robot.keyRelease(kc.code);
        if (kc.modifier != KeyEvent.VK_UNDEFINED) {
            robot.keyRelease(kc.modifier);
        }
    }
}
