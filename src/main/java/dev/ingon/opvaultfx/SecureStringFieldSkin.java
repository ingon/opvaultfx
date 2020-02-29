package dev.ingon.opvaultfx;

import javafx.beans.binding.DoubleBinding;
import javafx.beans.value.ObservableDoubleValue;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

class SecureStringFieldSkin  extends SkinBase<SecureStringField> {
    private final Pane group = new Pane();
    private final Text text = new Text();
    private final Path caret = new Path();
    private final Rectangle clip = new Rectangle();
    
    private final ObservableDoubleValue textRight;

    protected SecureStringFieldSkin(SecureStringField control) {
        super(control);
    
        textRight = new DoubleBinding() {
            { bind(group.widthProperty()); }
            @Override protected double computeValue() {
                return group.getWidth();
            }
        };

        clip.setSmooth(false);
        clip.setX(0);
        clip.widthProperty().bind(group.widthProperty());
        clip.heightProperty().bind(group.heightProperty());
        group.setClip(clip);
        
        group.getChildren().addAll(text, new Group(caret));
        getChildren().add(group);
        
        text.setManaged(false);
        
        caret.setManaged(false);
        caret.setStrokeWidth(1);
        
        text.caretShapeProperty().addListener(observable -> {
            caret.getElements().setAll(text.caretShapeProperty().get());
        });
        caret.opacityProperty().bind(new DoubleBinding() {
            { bind(control.focusedProperty()); }
            @Override protected double computeValue() {
                return control.focusedProperty().get() ? 1.0 : 0.0;
            }
        });

        group.getStyleClass().add("pane");
        text.getStyleClass().add("text");
        caret.getStyleClass().add("caret");

        control.len.addListener((__, ___, len) -> {
            if (len.intValue() == 0) {
                String err = getSkinnable().error.get();
                if (err == null) {
                    text.setFill(Color.rgb(255, 255, 255, 0.6));
                    text.setText("Enter master password");
                } else {
                    text.setFill(Color.web("#cf6679"));
                    text.setText(err);
                }
                return;
            }
            
            StringBuilder sb = new StringBuilder();
            for (int i = 0, n = len.intValue(); i < n; i++) {
                sb.append("\u25CF");
            }
            text.setFill(Color.rgb(255, 255, 255, 0.87));
            text.setText(sb.toString());
            updateXpos();
        });
        
        text.setFill(Color.rgb(255, 255, 255, 0.6));
        text.setText("Enter master password");
    }
    
    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        super.layoutChildren(contentX, contentY, contentWidth, contentHeight);
        
        final Bounds textNodeBounds = text.getLayoutBounds();
        final double ascent = text.getBaselineOffset();
        final double descent = textNodeBounds.getHeight() - ascent;
        final double textY = (ascent + group.getHeight() - descent) / 2;
        text.setY(textY);

        updateXpos();
    }
    
    private void updateXpos() {
        double textNodeWidth = text.getLayoutBounds().getWidth();
        if (textNodeWidth > textRight.get()) {
            text.setX(textRight.get() - textNodeWidth);
        } else {
            text.setX(0);
        }
        updateTextNodeCaretPos();
    }
    
    private void updateTextNodeCaretPos() {
        int pos = getSkinnable().len.intValue();
        text.setCaretPosition(pos);
        text.caretBiasProperty().set(true);
    }
}
