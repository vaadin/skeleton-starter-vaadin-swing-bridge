package com.example.swingbridge.views;

import com.example.swingbridge.ui.MainLayout;
import com.example.swingbridge.ui.SwingEditorView;
import com.jdimension.jlawyer.client.JKanzleiGUIBridge;
import com.jdimension.jlawyer.client.editors.files.ArchiveFileReviewsMissingPanelBridge;
import com.jdimension.jlawyer.client.events.MissingCalendarRowSelectionListener;
import com.jdimension.jlawyer.persistence.ArchiveFileBean;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;
import com.vaadin.modernization.swing.bridge.annotations.VaadinCallback;
import com.vaadin.modernization.swing.bridge.component.SwingBridge;

@PageTitle("Calendar / Missing")
@Route(value = "calendar/missing", layout = MainLayout.class)
public class CalendarMissingView extends SwingEditorView {

    /**
     * Captured for the adder/remover-style bridge method (returns
     * {@code Runnable}). Released in {@link #onDetach(DetachEvent)} to
     * avoid leaking proxy listeners on the Swing side across navigation
     * cycles.
     */
    private Registration registration;

    @Override
    protected void navigateSwing(JKanzleiGUIBridge gui) {
        gui.showCalendarMissing();
    }

    @Override
    protected void onSwingReady(JKanzleiGUIBridge gui) {
        registration = SwingBridge.interop().registerCallback(this,
                ArchiveFileReviewsMissingPanelBridge.class);
    }

    @Override
    protected void onDetach(DetachEvent event) {
        if (registration != null) {
            registration.remove();
            registration = null;
        }
        super.onDetach(event);
    }

    @VaadinCallback(observerFor = MissingCalendarRowSelectionListener.class)
    public void onMissingCalendarRowSelected(ArchiveFileBean caseBean) {
        String fileNumber = caseBean != null ? caseBean.getFileNumber()
                : "<unknown>";
        String title = caseBean != null ? caseBean.getName() : "";
        String body = title == null || title.isBlank()
                ? "Selected case: " + fileNumber
                : "Selected case: " + fileNumber + " — " + title;
        Notification n = Notification.show(body, 3500,
                Notification.Position.BOTTOM_END);
        n.addThemeVariants(NotificationVariant.LUMO_CONTRAST);
        n.setVisible(true);
    }
}
