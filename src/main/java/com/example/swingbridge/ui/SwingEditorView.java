package com.example.swingbridge.ui;

import java.awt.Component;
import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jdimension.jlawyer.bridge.JLawyerNavigation;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.modernization.swing.bridge.component.SwingBridge;
import com.vaadin.modernization.swing.graphics.SwingBridgeToolkit;

/**
 * Base class for Vaadin views that delegate to a Swing editor panel
 * via SwingBridge. Each subclass specifies which Swing view to show
 * by implementing {@link #onSwingReady(JLawyerNavigation)}.
 * <p>
 * SwingBridge handles single-instance per session: creating a new
 * SwingBridge component in each view reuses the existing Swing app.
 * <p>
 * A polling fallback ensures hideModuleBar + onSwingReady are called
 * even if the SwingBridge lifecycle hooks don't fire (e.g., when the
 * user takes a long time to log in and the JFrame lookup times out).
 */
public abstract class SwingEditorView extends VerticalLayout {

    private static final Logger LOG = LoggerFactory.getLogger(SwingEditorView.class);
    private static final String MAIN_CLASS = "com.jdimension.jlawyer.client.Main";

    private ScheduledExecutorService poller;
    private volatile boolean swingReady;

    /**
     * Called when the Swing app is ready. Runs on the correct Swing EDT.
     * Implement this to navigate to the desired Swing editor panel.
     */
    protected abstract void onSwingReady(JLawyerNavigation nav);

    public SwingEditorView() {
        setSizeFull();
    }

    @Override
    protected void onAttach(AttachEvent event) {
        super.onAttach(event);
        swingReady = false;

        SwingBridge bridge = createBridge();
        bridge.setSizeFull();
        removeAll();
        add(bridge);
        expand(bridge);

        // Polling fallback: covers the case where afterInit doesn't
        // fire (e.g., first view, user takes >5s to log in).
        // When afterInit DOES fire, it sets swingReady=true and the
        // poller stops on the next tick.
        scheduleSwingReadyCheck(bridge, event.getUI());
    }

    @Override
    protected void onDetach(DetachEvent event) {
        super.onDetach(event);
        shutdownPoller();
    }

    private void shutdownPoller() {
        if (poller != null && !poller.isShutdown()) {
            poller.shutdown();
        }
    }

    private void scheduleSwingReadyCheck(SwingBridge bridge, UI ui) {
        shutdownPoller();
        poller = Executors.newSingleThreadScheduledExecutor();
        poller.scheduleAtFixedRate(() -> {
            if (swingReady) {
                poller.shutdown();
                return;
            }
            try {
                ui.access(() -> {
                    Component component = bridge.getComponent();
                    if (component instanceof JLawyerNavigation nav) {
                        swingReady = true;
                        poller.shutdown();
                        SwingBridgeToolkit.targetThreadGroup(component)
                                .ifPresent(tg -> new Thread(tg, () ->
                                        EventQueue.invokeLater(() -> {
                                            nav.hideModuleBar();
                                            onSwingReady(nav);
                                        })
                                ).start());
                    }
                });
            } catch (Exception e) {
                // UI might be detached; poller will be shut down in onDetach
                LOG.debug("Polling check skipped", e);
                poller.shutdown();
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    private void dispatchToSwingEdt(Component component,
            JLawyerNavigation nav) {
        SwingBridgeToolkit.targetThreadGroup(component)
                .ifPresent(tg -> new Thread(tg, () ->
                        EventQueue.invokeLater(() -> {
                            nav.hideModuleBar();
                            onSwingReady(nav);
                        })
                ).start());
    }

    private SwingBridge createBridge() {
        return new SwingBridge(MAIN_CLASS) {
            @Override
            protected void beforeInit(Component component) {
                SwingBridgeToolkit.targetThreadGroup(component)
                        .ifPresent(threadGroup -> {
                            try {
                                EventQueue.invokeAndWait(() -> {
                                    System.clearProperty(
                                            "apple.awt.application.name");
                                    System.clearProperty(
                                            "com.apple.mrj.application.apple.menu.about.name");
                                    System.setProperty(
                                            "apple.laf.useScreenMenuBar",
                                            "false");
                                    UIManager.put(
                                            "apple.laf.useScreenMenuBar",
                                            Boolean.FALSE);
                                    var laf = UIManager.getLookAndFeel();
                                    try {
                                        UIManager.setLookAndFeel(
                                                laf.getClass().getName());
                                    } catch (Exception exc) {
                                        LOG.error("LAF reinstall error", exc);
                                    }
                                    SwingUtilities
                                            .updateComponentTreeUI(component);

                                    // Hide module bar synchronously before
                                    // init() starts the frame updater
                                    if (component instanceof JLawyerNavigation nav) {
                                        nav.hideModuleBar();
                                    }
                                });
                            } catch (InterruptedException
                                    | InvocationTargetException e) {
                                LOG.error("beforeInit error", e);
                                throw new RuntimeException(e);
                            }
                        });
            }

            @Override
            protected void afterInit(Component component) {
                if (component instanceof JLawyerNavigation nav) {
                    // Mark ready so the poller stops
                    swingReady = true;
                    dispatchToSwingEdt(component, nav);
                }
            }
        };
    }
}
