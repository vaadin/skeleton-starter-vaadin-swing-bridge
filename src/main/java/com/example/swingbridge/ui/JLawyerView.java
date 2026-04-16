/**
 * Copyright (C) 2026 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.example.swingbridge.ui;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import java.awt.Component;
import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.Route;
import com.vaadin.modernization.swing.bridge.component.SwingBridge;
import com.vaadin.modernization.swing.graphics.SwingBridgeToolkit;

@Menu(title = "J-Lawyer Client", order = 50)
@Route("jlawyer")
public class JLawyerView extends VerticalLayout {
    private static final Logger LOG = LoggerFactory
            .getLogger(JLawyerView.class);

    private static final class JLawyerApp extends SwingBridge {
        private static final String MAIN_CLASS = "com.jdimension.jlawyer.client.Main";

        private JLawyerApp() {
            super(MAIN_CLASS);
        }

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

                                System.setProperty("apple.laf.useScreenMenuBar",
                                        "false");
                                UIManager.put("apple.laf.useScreenMenuBar",
                                        Boolean.FALSE);

                                // Reinstall the same LAF to refresh
                                // defaults
                                var laf = UIManager.getLookAndFeel();
                                try {
                                    UIManager.setLookAndFeel(
                                            laf.getClass().getName());
                                } catch (Exception exc) {
                                    LOG.error("JLawyaer app run error", exc);
                                    exc.printStackTrace();
                                }

                                // Depending on project's code, we may need
                                // to update the already instantiated
                                // frames:
                                // for (Frame f : Frame.getFrames()) {
                                // if (f instanceof JFrame jf) {
                                SwingUtilities.updateComponentTreeUI(component);
                                // }
                                // }
                            });
                        } catch (InterruptedException
                                | InvocationTargetException e) {
                            LOG.error("JLawyaer app run error", e);
                            throw new RuntimeException(e);
                        }
                    });
        }
    }

    public JLawyerView() {
        setSizeFull();
        JLawyerApp jLawyer = new JLawyerApp();
        jLawyer.setSizeFull();
        add(jLawyer);
        expand(jLawyer);
    }
}
