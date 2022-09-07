import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import java.time._
import java.time.format._
import java.time.temporal.ChronoUnit._

object SystemService {
  def props(property: List[Property], reservationService: ActorRef): Props = Props(new SystemService(property,reservationService: ActorRef))
  def name(name: String = "SystemService"): String = name
}


class SystemService(property: List[Property], reservationService: ActorRef) extends Actor with ActorLogging {

  val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

  //Added the hardcoded reservations for testing purpose
  var reservationList: List[Reservation] = List(Reservation("griffin", 1, "21/07/2022", true),
    Reservation("hellobello",1,"25/07/2022",false),
    Reservation("bernard",2,"29/09/2022",false),
    Reservation("tony", 4, "01/09/2022", false),
    Reservation("xyz",5,"16/08/2022",true),
    Reservation("abx", 7, "17/09/2022", false))

  def receive : Receive = {
    case Search(propertyType: String, dateOfReservation: String) =>
      val propertyByType = property.filter(_.propertyType == propertyType)
      val availablePropertyId = propertyByType.map(i => i.propertyId.toInt) //get the ids of the available properties
      var availablePropertyIdsOnMentionedDate = availablePropertyId

      // map the ids of available properties to propertyMappedToDate and check if any date of reservation is empty

      for(eachReservation <- reservationList) {

        if (eachReservation.alreadyReservedDate.equals(dateOfReservation)) {
          availablePropertyIdsOnMentionedDate = availablePropertyIdsOnMentionedDate.filterNot(e => e == eachReservation.propertyId)
        }
      }

      var finalPropertyList = propertyByType.filter(i => availablePropertyIdsOnMentionedDate.contains(i.propertyId) )
      sender() ! SearchResults(finalPropertyList)

    case MakeReservation(clientName: String, dateOfReservation: String, propertyName: String,propertyId: Int,
    propertyType: String, propertyCategory: Int) =>

      val replyTo = sender()
      val temporaryService = context.actorOf(Props(new TemporaryService(property, reservationService,
        MakeReservation(clientName, dateOfReservation, propertyName, propertyId, propertyType, propertyCategory),
        reservationList, replyTo)))

    case ReservationConfirmation(reservation:Reservation) =>
      if (!reservationList.exists{
        r=> r.propertyId == reservation.propertyId && r.alreadyReservedDate.equals(reservation.alreadyReservedDate)
      }) {
        reservationList = reservationList :+ reservation
        sender() ! ReservationConfirmation(reservation)
      } else {
        sender() ! ReservationDeclined(reservation.clientName, reservation.propertyId, reservation.alreadyReservedDate)
      }

    case CancelReservation(clientName: String,propertyId: Int,alreadyReservedDate: String) =>
      val now = LocalDate.now()

      val oldSize = reservationList.size

      reservationList = reservationList.filterNot{r =>
        val reservedDate = LocalDate.parse(r.alreadyReservedDate, formatter)
        r.clientName.equals(clientName) && r.propertyId == propertyId && r.alreadyReservedDate.equals(alreadyReservedDate) && DAYS.between(now, reservedDate) > 2
      }

      if (reservationList.size < oldSize) {
        sender() ! CancellationConfirmation(clientName,propertyId,alreadyReservedDate)
      } else {
        sender() ! CancellationDeclined(clientName,propertyId,alreadyReservedDate)
      }

    case AcknowledgeReservation(reservation: Reservation) => {
      log.info("SYSTEM: reservation acknowledged")
    }
    case AcknowledgeCancellation(clientName: String,propertyId: Int,alreadyReservedDate: String) => {
      log.info("SYSTEM: cancellation acknowledged")
    }
  }


}

class TemporaryService(property: List[Property], reservationService: ActorRef,makeReservation: MakeReservation,
                       reservationlist:List[Reservation], replyTo: ActorRef ) extends Actor with ActorLogging {

  if(!reservationlist.exists{
    reservation => reservation.alreadyReservedDate.equals(makeReservation.dateOfReservation) &&
      reservation.propertyId == makeReservation.propertyId }) {
    reservationService ! makeReservation
  } else {
    replyTo ! ReservationDeclined(makeReservation.clientName, makeReservation.propertyId, makeReservation.dateOfReservation)
  }

  def receive: Receive =  {
    case ReservationConfirmation(reservation: Reservation) =>

      context.parent ! ReservationConfirmation(reservation)
      context.become(waitOnSystem)

  }

  def waitOnSystem: Receive = {

    case ReservationConfirmation(reservation: Reservation) =>
      replyTo ! ReservationFinalized(reservation, context.parent)
      context.stop(self)

    case ReservationDeclined(clientName: String,propertyId: Int,alreadyReservedDate: String) =>
      replyTo ! ReservationDeclined(clientName,propertyId,alreadyReservedDate)
      context.stop(self)

  }



}

