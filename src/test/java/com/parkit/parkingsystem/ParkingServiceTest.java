package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    private static ParkingService parkingService;

    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;

    @BeforeEach
    private void setUpPerTest() {
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
            Ticket ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber("ABCDEF");
            when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
            when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);

            when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
    }
     @Test
    public void testProcessIncomingVehicle() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1); // Simule voiture
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1); // Simule place de parking 1
        when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);  // Simule enregistrement du ticket

        parkingService.processIncomingVehicle();

        verify(ticketDAO, times(1)).saveTicket(any(Ticket.class)); // Verifie l'enregistrement ticket
        verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class)); // Verifie changement disponibilité place de parking 
    }

    @Test
    public void processExitingVehicleTestUnableUpdate() {
        Ticket ticket = new Ticket();  // Création de ticket
        ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));  // Heure d'enregistrement véhicule
        ticket.setVehicleRegNumber("ABCDEF");
        when(ticketDAO.getTicket("ABCDEF")).thenReturn(ticket); // Récupère le ticket d'enregistrement

        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false); // Erreuur d'enregistrement de ticket

        parkingService.processExitingVehicle();

        verify(ticketDAO, times(1)).updateTicket(any(Ticket.class)); // Vérifie l'enregistrement ticket
        verify(parkingSpotDAO, never()).updateParking(any(ParkingSpot.class)); // Vérifie la modification de la place de parking
    }

    @Test
    public void testGetNextParkingNumberIfAvailable() {
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1); // Simule disponibilité d'une place parking

        int parkingNumber = parkingService.getNextParkingNumberIfAvailable(); // Récupère le numéro de place de parking

        assertEquals(1, parkingNumber); // Vérifie le fonctionnement de la méthode
    }
}
