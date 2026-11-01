package citypulse.city;

public interface CityVisitor<R> {
    R visit(City city);

    R visit(District district);

    R visit(Building building);

    R visit(StreetLight light);

    R visit(TrafficSignal signal);

    R visit(AirMonitor monitor);

    R visit(WaterPump pump);

    R visit(ChargingStation station);
}
