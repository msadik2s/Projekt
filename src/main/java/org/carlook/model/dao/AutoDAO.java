package org.carlook.model.dao;

import com.vaadin.ui.Notification;
import org.carlook.model.objects.dto.ReservierungDTO;
import org.carlook.model.objects.dto.AutoDTO;
import org.carlook.model.objects.dto.EndkundeDTO;
import org.carlook.model.objects.dto.UserDTO;
import org.carlook.process.exceptions.DatabaseException;
import org.carlook.process.proxy.AutoControlProxy;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AutoDAO extends AbstractDAO {
    private static AutoDAO dao = null;

    private AutoDAO() {
    }

    public static AutoDAO getInstance() {
        if (dao == null) {
            dao = new AutoDAO();
        }
        return dao;
    }

    //Erzeugt die Autos, die ein Vertriebler erstellt hat
    public List<AutoDTO> getAutoList(UserDTO userDTO) throws SQLException {
        String sql = "SELECT marke, baujahr, beschreibung, auto_id " +
                "FROM carlook.auto " +
                "WHERE vertriebler_id = ? ;";
        PreparedStatement statement = this.getPreparedStatement(sql);
        ResultSet rs = null;
        try {
            statement.setInt(1, userDTO.getId());
            rs = statement.executeQuery();
        } catch (SQLException e) {
            Notification.show("9 Es ist ein SQL-Fehler aufgetreten. Bitte informieren Sie einen Administrator!");
        }
        List<AutoDTO> list = new ArrayList<>();
        assert rs != null;
        buildList(rs, list);
        return list;
    }


    //Erstellt ein neues Auto in der Datenbank
    public boolean createAuto(AutoDTO auto, UserDTO userDTO) {
        String sql = "INSERT INTO carlook.auto(marke, baujahr, beschreibung, auto_id, vertriebler_id) " +
                "VALUES (?, ?, ?, ?, ?)";

        PreparedStatement statement = this.getPreparedStatement(sql);

        try {
            int x=AutoDAO.getInstance().getMaxID()+1;
            statement.setString(1, auto.getMarke().toLowerCase());
            statement.setInt(2, Integer.parseInt(auto.getBaujahr()));
            statement.setString(3, auto.getBeschreibung().toLowerCase());
            statement.setInt(4, x);
            statement.setInt(5, userDTO.getId());
            statement.executeUpdate();
            return true;
        } catch (SQLException ex) {
            return false;
        }

    }

    //Verändert ein bestehendes Auto in der Datenbank
    public boolean updateAuto(AutoDTO auto) {
        String sql = "UPDATE carlook.auto " +
                "SET marke = ?, baujahr = ?, beschreibung = ?" +
                "WHERE carlook.auto.auto_id = ? ;";
        PreparedStatement statement = this.getPreparedStatement(sql);
        try {
            statement.setString(1, auto.getMarke());
            statement.setInt(2, Integer.parseInt(auto.getBaujahr()));
            statement.setString(3, auto.getBeschreibung());
            statement.setInt(4, auto.getAuto_id());
            statement.executeUpdate();
            return true;
        } catch (SQLException ex) {
            return false;
        }
    }


    //Löscht ein Auto aus der Datenbank
    public boolean deleteAuto(AutoDTO auto) {
        String sql = "DELETE " +
                "FROM carlook.auto " +
                "WHERE carlook.auto.auto_id = ? ;";
        PreparedStatement statement = this.getPreparedStatement(sql);
        try {
            statement.setInt(1, auto.getAuto_id());
            statement.executeUpdate();
            return true;
        } catch (SQLException ex) {
            return false;
        }
    }

    public List<AutoDTO> getAutoForSearch(String suchtext, String filter) throws SQLException {
        filter = filter.toLowerCase();
        suchtext=suchtext.toLowerCase();
        PreparedStatement statement;
        ResultSet rs = null;
        if (suchtext.equals("")) {
            String sql = "SELECT marke, baujahr, beschreibung, auto_id " +
                    "FROM carlook.auto; ";
            statement = this.getPreparedStatement(sql);
            try {
                rs = statement.executeQuery();
            } catch (SQLException e) {
                Notification.show("17 Es ist ein SQL-Fehler aufgetreten. Bitte informieren Sie einen Administrator!");
            }


        } else if (filter.equals("baujahr")) {
            String sql = "SELECT marke, baujahr, beschreibung, auto_id " +
                    "FROM carlook.auto " +
                    "WHERE " + filter + " = ? ;";
            statement = this.getPreparedStatement(sql);
            try {
                statement.setInt(1, Integer.parseInt(suchtext));
                rs = statement.executeQuery();
            } catch (SQLException e) {
                Notification.show("Es ist ein SQL-Fehler aufgetreten. Bitte informieren Sie einen Administrator!");
            }
        }

        else {
            String sql = "SELECT marke, baujahr, beschreibung, auto_id " +
                    "FROM carlook.auto " +
                    "WHERE " + filter + " like ? ;";
            statement = this.getPreparedStatement(sql);


            try {
                statement.setString(1, "%" + suchtext + "%");
                rs = statement.executeQuery();
            } catch (SQLException e) {
                Notification.show("19 Es ist ein SQL-Fehler aufgetreten. Bitte informieren Sie einen Administrator!");
            }
        }

        List<AutoDTO> list = new ArrayList<>();

        assert rs != null;
        buildList(rs, list);
        return list;
    }

    //Zeigt alle Autos an, die sich ein Endunde reserviert hat
    public List<AutoDTO> reserviereAuto(EndkundeDTO endkundeDTO) throws SQLException {
        String sql = "SELECT  marke, baujahr, beschreibung, auto_id " +
                "FROM carlook.auto " +
                "WHERE auto_id = ( SELECT auto_id " +
                "FROM carlook.reservierung_to_auto " +
                "WHERE reservierungs_id = ?);";
        PreparedStatement statement = this.getPreparedStatement(sql);
        List<ReservierungDTO> list = ReservierungDAO.getInstance().getReservierungForEndkunde(endkundeDTO);
        List<AutoDTO> listAuto = new ArrayList<>();
        ResultSet rs = null;
        for (ReservierungDTO reservierungDTO : list) {
            int id_bewerbung = reservierungDTO.getId();
            try {
                statement.setInt(1, id_bewerbung);
                rs = statement.executeQuery();
            } catch (SQLException e) {
                Notification.show("20 Es ist ein SQL-Fehler aufgetreten. Bitte informieren Sie einen Administrator!");
            }
            assert rs != null;
            buildList(rs, listAuto);
        }

        return listAuto;
    }

    private void buildList(ResultSet rs, List<AutoDTO> listStellenanzeige) throws SQLException {

        AutoDTO autoDTO;
        try {
            while (rs.next()) {

                autoDTO = new AutoDTO();
                autoDTO.setMarke(rs.getString(1));
                autoDTO.setBeschreibung(rs.getString(3));
                autoDTO.setAuto_id(rs.getInt(4));
                autoDTO.setBaujahr(Integer.parseInt(rs.getString(2)));

                listStellenanzeige.add(autoDTO);
            }
        } catch (SQLException e) {
            Notification.show("Es ist ein schwerer SQL-Fehler aufgetreten. Bitte informieren Sie einen Administrator!");
        } finally{
            assert rs != null;
            rs.close();
        }
    }




    public int getMaxID() throws SQLException {
        String sql = "SELECT max(auto_id) " +
                "FROM carlook.auto ;";
        PreparedStatement statement = getPreparedStatement(sql);
        ResultSet rs = null;

        try {
            rs = statement.executeQuery();
        } catch (SQLException throwables) {
            System.out.println("Fehler 1 bei addAuto");
        }

        int currentValue = 0;

        try {
            assert rs != null;
            rs.next();
            currentValue = rs.getInt(1);
        } catch (SQLException throwables) {
            System.out.println("Fehler 2 bei addAuto");
        } finally {
            assert rs != null;
            rs.close();
        }
        return currentValue;
    }


}


