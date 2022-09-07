import akka.actor.{Actor, ActorLogging, Props}

object ReservationService {
  def props(upgradeProbability: Double): Props = Props(new ReservationService(upgradeProbability))
  def name(name: String = "ReservationService"): String = name
}


class ReservationService(upgradeProbability: Double) extends Actor with ActorLogging{

  def receive: Receive = {

    case MakeReservation(clientName: String, dateOfReservation: String, propertyName: String,propertyId: Int,
    propertyType: String, propertyCategory: Int) =>

      sender ! ReservationConfirmation(Reservation(clientName,propertyId ,dateOfReservation, math.random() < upgradeProbability))


  }

}
