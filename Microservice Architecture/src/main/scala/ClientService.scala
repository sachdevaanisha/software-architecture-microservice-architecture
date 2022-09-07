import akka.actor.{Actor, ActorLogging, ActorRef, Props}

object ClientService {
  def props(client: Client, systemService: ActorRef): Props = Props(new ClientService(client, systemService: ActorRef))
  def name(client: Client) : String = client.clientName
}

class ClientService(client: Client, systemService: ActorRef) extends Actor with ActorLogging {
  def receive : Receive = {
    case Search(propertyType: String, dateOfReservation: String) =>
      systemService ! Search(propertyType, dateOfReservation)

    case SearchResults(availableProperty: List[Property]) =>
      log.info("Search Results: " + availableProperty.toString())

    case MakeReservation(clientName: String, dateOfReservation: String, propertyName: String, propertyId:Int, propertyType: String, propertyCategory: Int) =>
      systemService ! MakeReservation(clientName, dateOfReservation, propertyName,propertyId, propertyType, propertyCategory)

    case ReservationFinalized(reservation:Reservation, replyTo: ActorRef) =>
      replyTo ! AcknowledgeReservation(reservation)
      log.info("Reservation Confirmation: " + reservation.toString())

    case ReservationDeclined(clientName: String,propertyId: Int,alreadyReservedDate: String) =>
      log.info("Reservation Declined: (" + clientName + ", " + propertyId + ", " + alreadyReservedDate + ")")

    case CancelReservation(clientName: String,propertyId: Int,alreadyReservedDate: String) =>
      systemService ! CancelReservation(clientName,propertyId,alreadyReservedDate)

    case CancellationConfirmation(clientName: String,propertyId: Int,alreadyReservedDate: String) =>
      sender() ! AcknowledgeCancellation(clientName: String,propertyId: Int,alreadyReservedDate: String)
      log.info("Cancellation Confirmation: (" + clientName + ", " + propertyId + ", " + alreadyReservedDate + ")")

    case CancellationDeclined(clientName: String,propertyId: Int,alreadyReservedDate: String) =>
      log.info("Cancellation Declined: (" + clientName + ", " + propertyId + ", " + alreadyReservedDate + ")")
  }

}
