package org.carlook.gui.views;

import com.vaadin.event.selection.SelectionEvent;
import com.vaadin.event.selection.SelectionListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.*;
import org.carlook.gui.components.TopPanel;
import org.carlook.gui.ui.MyUI;
import org.carlook.gui.windows.CreateAutoWindow;
import org.carlook.gui.windows.DeleteAutoWindow;
import org.carlook.gui.windows.DeleteWindow;
import org.carlook.gui.windows.AutoWindow;
import org.carlook.model.objects.dto.AutoDTO;
import org.carlook.model.objects.dto.UserDTO;
import org.carlook.model.objects.dto.VertrieblerDTO;
import org.carlook.process.control.AutoControl;
import org.carlook.process.proxy.AutoControlProxy;
import org.carlook.process.proxy.SearchControlProxy;
import org.carlook.services.util.BuildGrid;
import org.carlook.services.util.Roles;

import java.sql.SQLException;
import java.util.List;

public class AutoView extends VerticalLayout implements View {

    private AutoDTO selektiert;
    private List<AutoDTO> list;

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {

        UserDTO user = (UserDTO) VaadinSession.getCurrent().getAttribute(Roles.CURRENT_USER);
        VertrieblerDTO vertrieblerDTO = new VertrieblerDTO(((MyUI) UI.getCurrent()).getUserDTO());
        this.setUp(vertrieblerDTO);
    }

    private void setUp(VertrieblerDTO vertrieblerDTO) {

        //Top Layer
        this.addComponent(new TopPanel());
        setStyleName("schrift-profil");
        Label line = new Label("<hr>", ContentMode.HTML);
        this.addComponent(line);
        line.setSizeFull();
        //Tabelle
        final Grid<AutoDTO> grid = new Grid<>("Ihre Autos");
        grid.setSizeFull();
        grid.setHeightMode(HeightMode.UNDEFINED);
        grid.setStyleName("schrift-tabelle");
        SingleSelect<AutoDTO> selection = grid.asSingleSelect();

        //Tabelle befüllen
        try {
            list = SearchControlProxy.getInstance().getAutoForUser();
        } catch (SQLException e) {
            Notification.show("2 Es ist ein SQL-Fehler aufgetreten. Bitte informieren Sie einen Administrator!");
        }
        BuildGrid.buildGrid(grid);
        grid.addColumn(AutoDTO::getAnzahl_res).setCaption("Anzahl der Reservierungen");
        grid.setItems(list);

        //ShowButton
        Button showButton = new Button("Bearbeiten");
        showButton.setEnabled(false);

        //CreateButton
        Button createButton = new Button("Erstellen");

        //DeleteButton
        Button deleteButton = new Button("Löschen");
        deleteButton.setEnabled(false);


        //Tabellen Select Config
        grid.addSelectionListener(new SelectionListener<AutoDTO>() {
            @Override
            public void selectionChange(SelectionEvent<AutoDTO> event) {
                if (selection.getValue() == null) {
                    showButton.setEnabled(false);
                    deleteButton.setEnabled(false);
                } else {
                    System.out.println("Zeile selektiert: " + selection.getValue());
                    selektiert = selection.getValue();
                    deleteButton.setEnabled(true);
                    showButton.setEnabled(true);
                }
            }
        });

        //ShowButton Config Stellenanzeige Bearbeiten
        showButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                AutoWindow window = new AutoWindow(selektiert, grid, vertrieblerDTO);
                UI.getCurrent().addWindow(window);
            }
        });

        //CreateButton Config
        createButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                CreateAutoWindow window = new CreateAutoWindow(new AutoDTO(), grid, vertrieblerDTO);
                UI.getCurrent().addWindow(window);
            }
        });

        //deleteButton Config
        deleteButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                DeleteAutoWindow deleteAutoWindow = new DeleteAutoWindow(selektiert);
                UI.getCurrent().addWindow(new DeleteWindow(deleteAutoWindow));
            }
        });

        //HorizontalLayout
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.addComponent(showButton);
        horizontalLayout.addComponent(createButton);
        horizontalLayout.addComponent(deleteButton);

        //Darstellung
        addComponent(grid);
        addComponent(horizontalLayout);
        setComponentAlignment(horizontalLayout, Alignment.MIDDLE_CENTER);
    }
}
