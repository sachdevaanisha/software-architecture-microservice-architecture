import akka.actor.ActorSystem
import akka.actor.{Actor, ActorLogging, ActorRef, Props}

object Main extends App {

  val camilo: Client = Client("camilo", 20, "XYZ01")
  val ahmed: Client = Client("ahmed", 18, "XYZ02")
  val coen: Client = Client("coen", 27, "XYZ03")
  val peter: Client = Client("peter", 25, "XYZ04")
  val beat: Client = Client("beat", 28, "XYZ05")
  val maxim: Client = Client("maxim", 20, "XYZ06")
  val bas: Client = Client("bas", 25, "XYZ07")
  val steve: Client = Client("steve", 21, "XYZ08")
  val momo: Client = Client("momo", 9, "XYZ09")
  val anisha: Client = Client("anisha", 20, "XYZ10")

  val propertyList = List(Property(1, "goodWellness", "resort", 3, "brussels: belgium"),
    Property(2, "grandWaters", "resort", 5, "dinant: belgium"),
    Property(3, "hotelJazz", "hotel", 3, "amsterdam: netherlands"),
    Property(4, "hotelIbis", "hotel", 4, "holland: netherlands"),
    Property(5, "treeHouse", "apartment", 4, "paris: france"),
    Property(6, "tinyHome", "apartment", 3, "paris: france"),
    Property(7, "sweetHouse", "apartment", 5, "brussels:  belgium")
  )

  val actorSystem: ActorSystem = ActorSystem("ReservationSystem")
  val reservationService = actorSystem.actorOf(ReservationService.props(0.25), ReservationService.name())
  val systemService = actorSystem.actorOf(SystemService.props(propertyList, reservationService), SystemService.name())
  val clientServiceAnisha = actorSystem.actorOf(ClientService.props(anisha, systemService), ClientService.name(anisha))
  val clientServiceCamilo = actorSystem.actorOf(ClientService.props(camilo, systemService), ClientService.name(camilo))
  val clientServiceCoen = actorSystem.actorOf(ClientService.props(coen, systemService), ClientService.name(coen))
  val clientServicePeter = actorSystem.actorOf(ClientService.props(peter, systemService), ClientService.name(peter))
  val clientServiceBeat = actorSystem.actorOf(ClientService.props(beat, systemService), ClientService.name(beat))
  val clientServiceMaxim = actorSystem.actorOf(ClientService.props(maxim, systemService), ClientService.name(maxim))
  val clientServiceBas = actorSystem.actorOf(ClientService.props(bas, systemService), ClientService.name(bas))
  val clientServiceSteve = actorSystem.actorOf(ClientService.props(steve, systemService), ClientService.name(steve))
  val clientServiceMomo = actorSystem.actorOf(ClientService.props(momo, systemService), ClientService.name(momo))
  val clientServiceAhmed = actorSystem.actorOf(ClientService.props(ahmed, systemService), ClientService.name(ahmed))

  println("Search test")
  clientServiceAhmed ! Search("hotel", "24/10/2021")
  // wait 5 seconds
  Thread.sleep(5000)

  println("Testing 10 concurrent reservations")
  clientServiceAhmed ! MakeReservation("ahmed", "24/10/2021", "hotelJazz", 3, "hotel", 3)
  clientServiceAhmed ! MakeReservation("ahmed", "24/09/2022", "goodWellness", 1, "resort", 3)
  clientServiceAnisha ! MakeReservation("anisha", "21/10/2022", "goodWellness", 1, "resort", 3)
  clientServiceCamilo ! MakeReservation("camilo", "20/09/2022", "grandWaters", 5, "resort", 5)
  clientServiceCoen ! MakeReservation("coen", "21/08/2022", "hotelJazz", 3, "hotel", 3)
  clientServicePeter ! MakeReservation("peter", "24/09/2022", "hotelIbis", 4, "hotel", 4)
  clientServiceBeat ! MakeReservation("beat", "26/09/2022", "treeHouse", 5, "apartment", 4)
  clientServiceMaxim ! MakeReservation("maxim", "25/09/2022", "tinyHome", 6, "apartment", 3)
  clientServiceBas ! MakeReservation("bas", "24/11/2023", "sweetHouse", 7, "apartment", 5)
  clientServiceSteve ! MakeReservation("steve", "20/01/2022", "goodWellness", 3, "resort", 3)
  clientServiceMomo ! MakeReservation("momo", "09/09/2022", "sweetHouse", 7, "apartment", 5)
  // wait 5 seconds
  Thread.sleep(5000)
  println("Testing a declined and confirmed cancellation")
  clientServiceAhmed ! CancelReservation("ahmed", 3, "24/10/2021")
  clientServiceAhmed ! CancelReservation("ahmed", 1, "24/09/2022")

  // wait 5 seconds
  Thread.sleep(5000)
  println("Testing remaking a declined and confirmed cancellation (only 1 should succeed)")
  clientServiceAhmed ! MakeReservation("momo", "24/10/2021", "hotelJazz", 3, "hotel", 3)
  clientServiceAhmed ! MakeReservation("anisha", "24/09/2022", "goodWellness", 1, "resort", 3)
}