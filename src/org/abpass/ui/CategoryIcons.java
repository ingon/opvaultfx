package org.abpass.ui;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.abpass.opvault.Item;
import org.abpass.opvault.Item.Category;

import javafx.scene.image.Image;

public class CategoryIcons {
    public enum Size {
        SMALL("_36dp"),
        BIG("_72dp");
        
        private final String suffix;
        
        private Size(String suffix) {
            this.suffix = suffix;
        }
    }
    protected static Image get(Item item, Size size) {
        return getCategoryIcon(item.getCategory(), size);
    }
    
    private static Image getCategoryIcon(Category c, Size size) {
        switch (c) {
        case CreditCard:
            return getIconFromFile("credit_card", size);
        case Identity:
            return getIconFromFile("identity", size);
        case Login:
            return getIconFromFile("account_box", size);
        case Password:
            return getIconFromFile("lock", size);
        case SoftwareLicense:
        case Membership:
        case OutdoorLicense:
        case Rewards:
        case SecureNote:
            return getIconFromFile("description", size);
        case BankAccount:
            return getIconFromFile("account_balance", size);
        case Email:
            return getIconFromFile("email", size);
        case Database:
        case Server:
            return getIconFromFile("devices", size);
        case Router:
            return getIconFromFile("router", size);
        case DriverLicense:
        case Passport:
        case SSN:
            return getIconFromFile("recent_actors", size);
        default:
            return null;
        }
    }
    
    private static Image getIconFromFile(String name, Size size) {
        try {
            return new Image(new FileInputStream("images/" + name + "_white" + size.suffix + ".png"));
        } catch (FileNotFoundException e) {
            return null;
        }
    }
}
