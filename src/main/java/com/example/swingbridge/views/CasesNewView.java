package com.example.swingbridge.views;

import java.util.concurrent.CompletableFuture;

import com.example.swingbridge.ui.MainLayout;
import com.example.swingbridge.ui.SwingEditorView;
import com.jdimension.jlawyer.client.JKanzleiGUIBridge;
import com.jdimension.jlawyer.client.events.CityChooser;
import com.jdimension.jlawyer.persistence.CityDataBean;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;
import com.vaadin.modernization.swing.bridge.annotations.Dispatch;
import com.vaadin.modernization.swing.bridge.annotations.VaadinCallback;
import com.vaadin.modernization.swing.bridge.component.SwingBridge;

/**
 * Cases ▸ New editor. Beyond rendering the Swing
 * {@code NewArchiveFilePanel}, registers the {@link CityChooser} callback
 * so the deeply-nested PLZ magnifier inside
 * {@code Beteiligte → + → + → "neuer Kontakt"} delegates ZIP/City picking
 * to a Vaadin {@link CityLovDialog} instead of opening the Swing
 * {@code CitySearchDialog}.
 * <p>
 * Registration is scoped to this route's lifecycle — moving away from
 * {@code /cases/new} unwires the chooser, so other routes (Calendar, Mail,
 * etc.) don't carry chooser plumbing they don't need. {@code /cases/search}
 * would need its own copy if its Beteiligte flow is exercised.
 */
@PageTitle("Cases / New")
@Route(value = "cases/new", layout = MainLayout.class)
public class CasesNewView extends SwingEditorView {

    private Registration cityChooserRegistration;

    @Override
    protected void navigateSwing(JKanzleiGUIBridge gui) {
        gui.showCasesNew();
    }

    @Override
    protected void onSwingReady(JKanzleiGUIBridge gui) {
        cityChooserRegistration = SwingBridge.interop()
                .of(JKanzleiGUIBridge.class)
                .registerCallback(this);
    }

    @Override
    protected void onDetach(DetachEvent event) {
        if (cityChooserRegistration != null) {
            cityChooserRegistration.remove();
            cityChooserRegistration = null;
        }
        super.onDetach(event);
    }

    /**
     * Handles Swing-side ZIP/City picker requests by opening a Vaadin
     * {@link CityLovDialog}. Returns the future synchronously to the
     * Swing thread (via {@code Dispatch.ACCESS_SYNCHRONOUSLY}); the
     * future itself completes later when the user picks or cancels.
     */
    @VaadinCallback(observerFor = CityChooser.class,
            dispatch = Dispatch.ACCESS_SYNCHRONOUSLY)
    public CompletableFuture<CityDataBean> choose(String currentPlz) {
        CompletableFuture<CityDataBean> result = new CompletableFuture<>();
        new CityLovDialog(currentPlz, result::complete).open();
        return result;
    }
}
