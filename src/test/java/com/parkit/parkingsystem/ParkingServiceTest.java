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
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    Ticket ticket;
    @BeforeEach
    public void setUpPerTest() {
        try {

            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
            ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber("ABCDEF");
/*
            when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
            when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
*/

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
    public void processExitingVehicleTest() throws Exception {
        testProcessIncomingVehicle();
        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);

        ticket.setOutTime((new Date(System.currentTimeMillis() - (120 * 60 * 1000))));  // Heure d'enregistrement véhicule
        ticket.setVehicleRegNumber("ABCDEF");
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);

        parkingService.processExitingVehicle();
        verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class));
    }

    @Test
    public void processExitingVehicleTestUnableUpdate() throws Exception {
        testProcessIncomingVehicle();
        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
        ticket.setOutTime((new Date(System.currentTimeMillis() - (120 * 60 * 1000))));  // Heure d'enregistrement véhicule
        ticket.setVehicleRegNumber("ABCDEF");
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);

        parkingService.processExitingVehicle();
        verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class)); // Vérifie la modification de la place de parking
    }

    @Test
    public void testGetNextParkingNumberIfAvailable() {
        when(inputReaderUtil.readSelection()).thenReturn(1); // Simule voiture
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1); // Simule disponibilité d'une place parking

        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable(); // Récupère le numéro de place de parking

        /* Vérifie que la méthode retourne bien un ParkingSpot disponible */
        assertNotNull(parkingSpot);
        assertEquals(1, parkingSpot.getId());
        verify(parkingSpotDAO, times(1)).getNextAvailableSlot(any(ParkingType.class)); // Vérifie le fonctionnement de la méthode
    }

    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberNotFound() {
        when(inputReaderUtil.readSelection()).thenReturn(1); // Simule voiture
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(0); // Simule disponibilité d'une place parking

        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable(); // Récupère le numéro de place de parking

        assertNull(parkingSpot); // Vérifie que le résultat est null

        verify(parkingSpotDAO, times(1)).getNextAvailableSlot(any(ParkingType.class)); // Vérifie le fonctionnement de la méthode
    }

    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument() {
        // Simulation d'une saisie erronée (par exemple, 3 qui n'est pas géré)
        when(inputReaderUtil.readSelection()).thenReturn(3);

        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();

        // Vérifie que la méthode renvoie null sans interroger le DAO
        assertNull(parkingSpot);
        verify(parkingSpotDAO, never()).getNextAvailableSlot(any(ParkingType.class));
    }

    @Test
    public void testProcessIncomingVehicleBike() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(2); // Simule moto
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.BIKE)).thenReturn(1); // Simule place de parking 1
        when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);  // Simule enregistrement du ticket

        parkingService.processIncomingVehicle();

        verify(ticketDAO, times(1)).saveTicket(any(Ticket.class)); // Verifie l'enregistrement ticket
        verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class)); // Verifie changement disponibilité place de parking
    }

    @Test
    public void processExitingVehicleTestBike() throws Exception {
        testProcessIncomingVehicleBike();
        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);

        ticket.setOutTime((new Date(System.currentTimeMillis() - (120 * 60 * 1000))));  // Heure d'enregistrement véhicule
        ticket.setVehicleRegNumber("ABCDEF");
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);

        parkingService.processExitingVehicle();
        verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class));
    }

    @Test
    public void processExitingVehicleTestUnableUpdateBike() throws Exception {
        testProcessIncomingVehicleBike();
        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
        ticket.setOutTime((new Date(System.currentTimeMillis() - (120 * 60 * 1000))));  // Heure d'enregistrement véhicule
        ticket.setVehicleRegNumber("ABCDEF");
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);

        parkingService.processExitingVehicle();
        verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class)); // Vérifie la modification de la place de parking
    }

    @Test
    public void testGetNextParkingNumberIfAvailableBike() {
        when(inputReaderUtil.readSelection()).thenReturn(2); // Simule moto
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1); // Simule disponibilité d'une place parking

        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable(); // Récupère le numéro de place de parking

        /* Vérifie que la méthode retourne bien un ParkingSpot disponible */
        assertNotNull(parkingSpot);
        assertEquals(1, parkingSpot.getId());
        verify(parkingSpotDAO, times(1)).getNextAvailableSlot(any(ParkingType.class)); // Vérifie le fonctionnement de la méthode
    }

    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberNotFoundBike() {
        when(inputReaderUtil.readSelection()).thenReturn(2); // Simule moto
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(0); // Simule disponibilité d'une place parking

        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable(); // Récupère le numéro de place de parking

        assertNull(parkingSpot); // Vérifie que le résultat est null

        verify(parkingSpotDAO, times(1)).getNextAvailableSlot(any(ParkingType.class)); // Vérifie le fonctionnement de la méthode
    }
}
