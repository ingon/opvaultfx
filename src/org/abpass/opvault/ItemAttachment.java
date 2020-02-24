package org.abpass.opvault;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Arrays;

import org.abpass.opvault.ItemAttachmentException.ItemAttachmentDataException;
import org.abpass.opvault.ItemAttachmentException.ItemAttachmentIconException;
import org.abpass.opvault.ItemAttachmentException.ItemAttachmentInvalidHeaderException;
import org.abpass.opvault.ItemAttachmentException.ItemAttachmentInvalidMetadataException;
import org.abpass.opvault.ItemAttachmentException.ItemAttachmentOverviewException;
import org.abpass.opvault.ItemAttachmentException.ItemAttachmentOverviewParseException;
import org.abpass.opvault.ProfileException.ProfileKeysException;
import org.abpass.opvault.ProfileException.ProfileLockedException;

import dev.ingon.json.zero.ParseException;
import dev.ingon.json.zero.hl.JsonParser;
import dev.ingon.json.zero.hl.JsonTypedHandler;

public class ItemAttachment {
    private static final int HEADER_MIN_SIZE = 16;
    private static final byte[] HEADER = new byte[]{'O', 'P', 'C', 'L', 'D', 'A', 'T'};
    private static final int HEADER_SIZE = HEADER.length;

    static void loadAttachment(Item item, Path path, byte[] input) throws ItemAttachmentInvalidHeaderException, ItemAttachmentInvalidMetadataException {
        if (input.length < HEADER_MIN_SIZE) { // header length
            throw new ItemAttachmentInvalidHeaderException(path);
        }
        
        if (! Arrays.equals(input, 0, HEADER_SIZE, HEADER, 0, HEADER_SIZE)) {
            throw new ItemAttachmentInvalidHeaderException(path);
        }
        
        var bb = ByteBuffer.wrap(input, HEADER_SIZE, HEADER_MIN_SIZE - HEADER_SIZE);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        
        var version = bb.get();
        var metadataLen = bb.getShort();
        bb.getShort(); // skip junk
        var iconLen = bb.getInt();
        
        byte[] metedataBytes = Arrays.copyOfRange(input, HEADER_MIN_SIZE, HEADER_MIN_SIZE + metadataLen);
        char[] metadataChars = Security.decode(metedataBytes);
        
        ItemAttachment attachment;
        try {
            attachment = JsonParser.parse(metadataChars, newParser(item));
        } catch (ParseException e) {
            throw new ItemAttachmentInvalidMetadataException(path, e);
        }
        
        attachment.version = version;
        attachment.icon = Arrays.copyOfRange(input, HEADER_MIN_SIZE + metadataLen, HEADER_MIN_SIZE + metadataLen + iconLen);
        attachment.data = Arrays.copyOfRange(input, HEADER_MIN_SIZE + metadataLen + iconLen, input.length);

        item.addAttachment(attachment);
    }
    
    static JsonTypedHandler<ItemAttachment> newParser(Item item) {
        Json<ItemAttachment> handler = new Json<ItemAttachment>(() -> new ItemAttachment(item));
        
        handler.stringProperty("uuid", (t, o) -> t.uuid = o);
        handler.stringProperty("itemUUID", (t, o) -> t.itemUUID = o);
        handler.numberProperty("contentsSize", (t, o) -> t.contentsSize = o.intValue());
        handler.booleanProperty("external", (t, o) -> System.out.println("external: " + o)); // TODO ignore?

        handler.instantProperty("createdAt", (t, o) -> t.createdAt = o);
        handler.instantProperty("updatedAt", (t, o) -> t.updatedAt = o);
        handler.instantProperty("txTimestamp", (t, o) -> t.tx = o);
        
        handler.base64Property("overview", (t, o) -> t.overview = o);

        return handler;
    }
    
    private final Item item;
    
    private String uuid;
    private String itemUUID;
    private int contentsSize;

    private Instant createdAt;
    private Instant updatedAt;
    private Instant tx;
    
    private byte[] overview;
    
    private byte version;
    private byte[] icon;
    private byte[] data;
    
    public ItemAttachment(Item item) {
        this.item = item;
    }

    public String getUUID() {
        return uuid;
    }
    
    public String getItemUUID() {
        return itemUUID;
    }
    
    public int getContentsSize() {
        return contentsSize;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public Instant getUpdatedAt() {
        return updatedAt;
    }
    
    public Instant getTx() {
        return tx;
    }
    
    public ItemAttachmentOverview getOverview() throws ProfileLockedException, ItemAttachmentOverviewException, ItemAttachmentOverviewParseException {
        try (var keys = item.overviewKeys()) {
            char[] overview = keys.decryptData(this.overview);
            try {
                return JsonParser.parse(overview, ItemAttachmentOverview.newParser());
            } catch (ParseException e) {
                throw new ItemAttachmentOverviewParseException(e);
            } finally {
                Security.wipe(overview);
            }
        } catch (OPDataException | ProfileKeysException e) {
            throw new ItemAttachmentOverviewException(e);
        }
    }

    public byte[] getIcon() throws ItemAttachmentIconException { // TODO maybe control this better
        if (this.version == 1) {
            try (var keys = item.keys()) {
                return keys.decryptRawData(this.icon);
            } catch (ProfileLockedException | ProfileKeysException | OPDataException e) {
                throw new ItemAttachmentIconException(e);
            }
        }
        
        return this.icon; // TODO none of the keys seems to work for v2
    }
    
    public byte[] getData() throws ItemAttachmentDataException {
        try (var keys = item.keys()) {
            return keys.decryptRawData(this.data);
        } catch (ProfileLockedException | ProfileKeysException | OPDataException e) {
            throw new ItemAttachmentDataException(e);
        }
    }
}
