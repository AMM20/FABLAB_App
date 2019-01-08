package martinez.alex.lopez.laura.upc.fablabapp;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Reserva implements Serializable {

    // Declaració dels camps de la classe Client.

    private String projectUse, serviceType, material, thickness, totalCost, reservationID;
    private Date date;
    private List<String> reservedHours;
    private Client client;

    // Getters i Setters

    public String getProjectUse() {
        return projectUse;
    }

    public void setProjectUse(String projectUse) {
        this.projectUse = projectUse;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public String getThickness() {
        return thickness;
    }

    public void setThickness(String thickness) {
        this.thickness = thickness;
    }

    public String getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(String totalCost) {
        this.totalCost = totalCost;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public List<String> getReservedHours() {
        return reservedHours;
    }

    public void setReservedHours(List<String> reservedHours) {
        this.reservedHours = reservedHours;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public String getReservationID() {
        return reservationID;
    }

    public void setReservationID(String reservationID) {
        this.reservationID = reservationID;
    }


}
