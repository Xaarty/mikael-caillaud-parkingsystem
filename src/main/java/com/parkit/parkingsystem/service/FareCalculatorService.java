package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

// public class FareCalculatorService {

//     public void calculateFare(Ticket ticket){
//         if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
//             throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
//         }

//         int inHour = ticket.getInTime().getHours();
//         int outHour = ticket.getOutTime().getHours();

//         //TODO: Some tests are failing here. Need to check if this logic is correct
//         int duration = outHour - inHour;

//         switch (ticket.getParkingSpot().getParkingType()){
//             case CAR: {
//                 ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR);
//                 break;
//             }
//             case BIKE: {
//                 ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR);
//                 break;
//             }
//             default: throw new IllegalArgumentException("Unkown Parking Type");
//         }
//     }
// }

public class FareCalculatorService {

    public void calculateFare(Ticket ticket){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        long inTime = ticket.getInTime().getTime(); //long pour gérer les valeur numérique importante
        long outTime = ticket.getOutTime().getTime();
        double durationInHours = (double) (outTime - inTime) / (60 * 60 * 1000); // divise par la durée d'une heure en millisecondes
                                                                                 // double pour éviter el troncage 
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
    }
}