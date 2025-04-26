package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;


public class FareCalculatorService {

    public void calculateFare(Ticket ticket, boolean discount){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        long inTime = ticket.getInTime().getTime(); //long pour gérer les valeur numérique importante
        long outTime = ticket.getOutTime().getTime();
        double durationInHours = (double) (outTime - inTime) / (60 * 60 * 1000); // divise par la durée d'une heure en millisecondes
                                                                                 // double pour éviter le troncage 
        if (durationInHours <= 0.5) {
        ticket.setPrice(0.0); // Parking gratuit
        return;
        }

        switch (ticket.getParkingSpot().getParkingType()) {
            case CAR: {
                ticket.setPrice(durationInHours * Fare.CAR_RATE_PER_HOUR);
                break;
            }
            case BIKE: {
                ticket.setPrice(durationInHours * Fare.BIKE_RATE_PER_HOUR);
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown Parking Type");
        }

        if (discount) {
            double discountedPrice = ticket.getPrice() * 0.95; // Appliquer 5% de réduction
            ticket.setPrice(discountedPrice); // Mettre à jour le prix du ticket
        }
    }
}