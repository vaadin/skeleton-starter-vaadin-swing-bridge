package com.example.swingbridge.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.swingbridge.SwingBridge;

@Route(value = "")
public class WarehouseView extends VerticalLayout {
    private static final String MAIN_CLASS = "com.mycompany.swingapp.warehouse.WarehouseInventoryApp3";

    public WarehouseView() {
        add(new SwingBridge(MAIN_CLASS));
        add(new Button(() -> {
            SwingBridge.interop().of(com.mycompany.swingapp.warehouse.WarehouseInventoryApp3Bridge.class)
                    .requestAsync(pWarehouseInventoryApp3Bridge -> pWarehouseInventoryApp3Bridge.selectForm("Offers"));
            return null;
        }));
    }

}
