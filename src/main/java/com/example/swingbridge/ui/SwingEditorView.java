package com.example.swingbridge.ui;

import java.awt.Component;
import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jdimension.jlawyer.client.JKanzleiGUIBridge;
import com.jdimension.jlawyer.client.JKanzleiGUIBridgeMetadata;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.BeforeLeaveEvent;
import com.vaadin.flow.router.BeforeLeaveEvent.ContinueNavigationAction;
import com.vaadin.flow.router.BeforeLeaveObserver;
import com.vaadin.modernization.swing.bridge.component.SwingBridge;
import com.vaadin.modernization.swing.bridge.interop.BridgeHandle;
import com.vaadin.modernization.swing.graphics.SwingBridgeToolkit;

/**
 * Base class for Vaadin views that delegate to a Swing editor panel via
 * SwingBridge.
 * <p>
 * Subclasses implement {@link #navigateSwing(JKanzleiGUIBridge)} to call
 * the Swing show* method that performs the actual editor swap once Vaadin
 * navigation has been approved.
 * <p>
 * As a {@link BeforeLeaveObserver} the base class postpones every Vaadin
 * route change, asks Swing whether it's OK to leave the current editor via
 * {@code gui.confirmLeaveCurrentEditor()}, and only proceeds when Swing
 * reports success — so the validation / save-before-exit dialogs that live
 * in {@code EditorsRegistry.canLeaveCurrentEditor()} now also gate Vaadin-
 * driven navigation.
 * <p>
 * For the very first navigation in a session (no current Swing editor)
 * the bridge isn't ready yet, so {@code beforeLeave} skips the
 * postpone-and-await flow.
 */
public abstract class SwingEditorView extends VerticalLayout
        implements BeforeLeaveObserver, BeforeEnterObserver {

    private static final Logger LOG = LoggerFactory
            .getLogger(SwingEditorView.class);

    /**
     * Set by {@link #cancelNavigation} to signal the target view's
     * {@link #beforeEnter(BeforeEnterEvent)} to forward back to the source.
     * We have to call {@code action.proceed()} to unstick Vaadin's client-
     * side router (postpone-without-proceed silently drops subsequent
     * {@code RouterLink} clicks), but we don't want the target to actually
     * attach — that would attach a different Swing editor and cause a
     * visible flicker.
     */
    private static volatile Class<? extends com.vaadin.flow.component.Component> pendingCancelRedirect;

    private final SwingBridge bridge;

    /**
     * One-shot guard: set when the cancel path triggers a re-navigation to
     * ourselves. The re-fired beforeLeave consumes this flag and passes
     * through without re-running the leave check.
     */
    private boolean cancelInProgress = false;

    /**
     * Implemented by each leaf view to call its Swing show* method. Called
     * from {@link #onAttach} after Vaadin navigation has already been
     * approved by {@code beforeLeave}.
     */
    protected abstract void navigateSwing(JKanzleiGUIBridge gui);

    /**
     * Optional hook for post-attach setup (e.g.
     * {@code interop().registerCallback(...)}). Default: no-op.
     */
    protected void onSwingReady(JKanzleiGUIBridge gui) {
    }

    public SwingEditorView() {
        setSizeFull();
        bridge = new SwingBridge(
                JKanzleiGUIBridgeMetadata.SWING_MAIN_CLASS) {
            @Override
            protected void beforeInit(Component component) {
                applyMacOsLafWorkaround(component);
            }
        };
        bridge.setSizeFull();
        add(bridge);
        expand(bridge);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Class<? extends com.vaadin.flow.component.Component> redirect = pendingCancelRedirect;
        if (redirect != null) {
            // The source view's cancel path proceeded the postponed action
            // to unstick Vaadin's client; forward back to source so the
            // target never actually attaches.
            pendingCancelRedirect = null;
            event.forwardTo(redirect);
        }
    }

    @Override
    public void beforeLeave(BeforeLeaveEvent event) {
        if (consumeReentrantCancel()) {
            return;
        }
        if (!bridgeReady()) {
            return;
        }
        awaitSwingApproval(event);
    }

    private boolean consumeReentrantCancel() {
        if (cancelInProgress) {
            cancelInProgress = false;
            return true;
        }
        return false;
    }

    private boolean bridgeReady() {
        return bridge().isReady();
    }

    private void awaitSwingApproval(BeforeLeaveEvent event) {
        ContinueNavigationAction action = event.postpone();
        bridge().requestAsync(JKanzleiGUIBridge::confirmLeaveCurrentEditor)
                .whenComplete((approved, err) -> {
                    if (err != null) {
                        LOG.error("[bridge] confirmLeave failed", err);
                        return;
                    }
                    getUI().ifPresent(ui -> ui.access(
                            () -> applyDecision(approved, action)));
                });
    }

    private static BridgeHandle<JKanzleiGUIBridge> bridge() {
        return SwingBridge.interop().of(JKanzleiGUIBridge.class);
    }

    private void applyDecision(Boolean approved,
            ContinueNavigationAction action) {
        if (Boolean.TRUE.equals(approved)) {
            action.proceed();
        } else {
            cancelNavigation(action);
        }
    }

    @SuppressWarnings("unchecked")
    private void cancelNavigation(ContinueNavigationAction action) {
        // Cancel: we MUST call action.proceed() to unstick Vaadin's
        // client-side router (postpone-without-proceed silently drops
        // subsequent RouterLink clicks). pendingCancelRedirect tells the
        // target's beforeEnter to forwardTo back here, so the target never
        // renders.
        pendingCancelRedirect =
                (Class<? extends com.vaadin.flow.component.Component>) getClass();
        cancelInProgress = true;
        action.proceed();
    }

    @Override
    protected void onAttach(AttachEvent event) {
        super.onAttach(event);
        SwingBridge.interop()
                .onReady(JKanzleiGUIBridge.class, gui -> {
                    gui.hideModuleBar();
                    navigateSwing(gui);
                    onSwingReady(gui);
                });
    }

    private static void applyMacOsLafWorkaround(Component component) {
        SwingBridgeToolkit.targetThreadGroup(component).ifPresent(
                threadGroup -> {
                    try {
                        EventQueue.invokeAndWait(() -> {
                            System.clearProperty(
                                    "apple.awt.application.name");
                            System.clearProperty(
                                    "com.apple.mrj.application.apple.menu.about.name");
                            System.setProperty(
                                    "apple.laf.useScreenMenuBar", "false");
                            UIManager.put("apple.laf.useScreenMenuBar",
                                    Boolean.FALSE);
                            var laf = UIManager.getLookAndFeel();
                            try {
                                UIManager.setLookAndFeel(
                                        laf.getClass().getName());
                            } catch (Exception exc) {
                                LOG.error("LAF reinstall error", exc);
                            }
                            SwingUtilities.updateComponentTreeUI(component);
                        });
                    } catch (InterruptedException
                            | InvocationTargetException e) {
                        LOG.error("beforeInit error", e);
                        throw new RuntimeException(e);
                    }
                });
    }
}
