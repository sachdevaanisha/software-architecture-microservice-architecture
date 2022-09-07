import akka.actor.ActorRef

case class Search(var propertyType: String,
                  var dateOfReservation: String)

case class SearchResults(var availableProperty: List[Property])

case class MakeReservation(var clientName: String,
                           var dateOfReservation: String,
                           var propertyName: String,
                           var propertyId: Int,
                           var propertyType: String,
                           var propertyCategory: Int)

case class ReservationConfirmation(var reservation: Reservation)

case class ReservationFinalized(var reservation: Reservation, replyTo: ActorRef)

case class AcknowledgeReservation(var reservation: Reservation)

case class ReservationDeclined(clientName: String,propertyId: Int,alreadyReservedDate: String)

case class CancelReservation(clientName: String,propertyId: Int,alreadyReservedDate: String)

case class CancellationConfirmation(clientName: String,propertyId: Int,alreadyReservedDate: String)

case class AcknowledgeCancellation(clientName: String,propertyId: Int,alreadyReservedDate: String)

case class CancellationDeclined(clientName: String,propertyId: Int,alreadyReservedDate: String)