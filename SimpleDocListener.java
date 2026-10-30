import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

// lets us write "field.getDocument().addDocumentListener(new SimpleDocListener(this::method))"
// instead of implementing all 3 DocumentListener methods every time
public class SimpleDocListener implements DocumentListener {
    private final Runnable action;

    public SimpleDocListener(Runnable action) {
        this.action = action;
    }

    @Override
    public void insertUpdate(DocumentEvent e) { action.run(); }

    @Override
    public void removeUpdate(DocumentEvent e) { action.run(); }

    @Override
    public void changedUpdate(DocumentEvent e) { action.run(); }
}
