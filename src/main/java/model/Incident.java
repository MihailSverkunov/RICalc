package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.apache.poi.ss.usermodel.Row;

@Getter
@Setter
public class Incident implements Comparable<Incident> {

    private static final double EARTH_RADIUS = 6378137;

    private String data;
    private Double number;
    private String type;
    private Double died;
    private Double wounded;
    private String district;
    private String region;
    private String locality;
    private String valueRoad;
    private String road;
    private String km;
    private String street;
    private String home;
    private Double latitude;
    private Double longitude;
    private List<String> closeIds = new ArrayList<>();
    private List<String> mediumIds = new ArrayList<>();


    private Map<Double, Double> distanceList = new HashMap<>();

    private boolean inCloseRange = false;

    private boolean inMediumRange = false;

    private boolean inRange = false;


    public boolean fillIncident(Row row) {
        if (
            Optional.ofNullable(row.getCell(13)).map(Objects::toString).filter(value -> !value.isEmpty()).isEmpty() ||
                Optional.ofNullable(row.getCell(14)).map(Objects::toString).filter(value -> !value.isEmpty()).isEmpty() ||
                Double.parseDouble(row.getCell(3).toString()) == 0) {
            return false;
        }

        data = row.getCell(0).toString();
        number = Double.valueOf(row.getCell(1).toString());
        type = row.getCell(2).toString();
        died = Double.valueOf(row.getCell(3).toString());
        wounded = Double.valueOf(row.getCell(4).toString());
        district = row.getCell(5).toString();
        region = row.getCell(6).toString();
        locality = Optional.ofNullable(row.getCell(7)).map(Object::toString).orElse(null);
        valueRoad = row.getCell(8).toString();
        road = Optional.ofNullable(row.getCell(9)).map(Object::toString).orElse(null);
        km = Optional.ofNullable(row.getCell(10)).map(Object::toString).orElse(null);
        street = Optional.ofNullable(row.getCell(11)).map(Object::toString).orElse(null);
        home = Optional.ofNullable(row.getCell(12)).map(Object::toString).orElse(null);

        latitude = Double.valueOf(row.getCell(13).toString());
        longitude = Double.valueOf(row.getCell(14).toString());
        return true;
    }

    public void fillRow(Row row, Map.Entry<Double, Double> distance) {

        row.createCell(0).setCellValue(data);
        row.createCell(1).setCellValue(String.format("%.0f", number));
        row.createCell(2).setCellValue(type);
        row.createCell(3).setCellValue(String.format("%.0f", died));
        row.createCell(4).setCellValue(String.format("%.0f", wounded));
        row.createCell(5).setCellValue(district);
        row.createCell(6).setCellValue(region);
        row.createCell(7).setCellValue(locality);
        row.createCell(8).setCellValue(valueRoad);
        row.createCell(9).setCellValue(road);
        row.createCell(10).setCellValue(km);
        row.createCell(11).setCellValue(street);
        row.createCell(12).setCellValue(home);
        row.createCell(13).setCellValue(String.format("%f", latitude));
        row.createCell(14).setCellValue(String.format("%f", longitude));
        row.createCell(16).setCellValue(String.format("%f", distance.getKey()));
        row.createCell(17).setCellValue(String.format("%.0f", distance.getValue()));

    }

    public void fillRow(Row row) {

        row.createCell(0).setCellValue(data);
        row.createCell(1).setCellValue(String.format("%.0f", number));
        row.createCell(2).setCellValue(type);
        row.createCell(3).setCellValue(String.format("%.0f", died));
        row.createCell(4).setCellValue(String.format("%.0f", wounded));
        row.createCell(5).setCellValue(district);
        row.createCell(6).setCellValue(region);
        row.createCell(7).setCellValue(locality);
        row.createCell(8).setCellValue(valueRoad);
        row.createCell(9).setCellValue(road);
        row.createCell(10).setCellValue(km);
        row.createCell(11).setCellValue(street);
        row.createCell(12).setCellValue(home);
        row.createCell(13).setCellValue(String.format("%f", latitude));
        row.createCell(14).setCellValue(String.format("%f", longitude));
        row.createCell(16).setCellValue(String.join(", ", closeIds));
        row.createCell(17).setCellValue(String.join(", ", mediumIds));

    }

    private static Double toRadian(Double coordinate) {
        return coordinate * Math.PI / 180.0;
    }

    @Override
    public int compareTo(Incident anotherIncident) {
        return latitude.compareTo(anotherIncident.latitude);
    }


    public Integer calculateDistance(Incident nextIncident, Integer rangeId) {
        Double radLatMain = toRadian(this.latitude);
        Double radLatNext = toRadian(nextIncident.getLatitude());

        double latDiff = radLatMain - radLatNext;
        double lonDiff = toRadian(this.longitude) - toRadian(nextIncident.getLongitude());
        Double distance = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(latDiff / 2), 2)
                                                      + Math.cos(radLatMain) * Math.cos(radLatNext) *
            Math.pow(Math.sin(lonDiff / 2), 2)));
        distance = distance * EARTH_RADIUS;

        if (distance <= 1000) {

            this.inRange = true;
            nextIncident.setInRange(true);

            this.distanceList.put(distance, nextIncident.getNumber());
            nextIncident.getDistanceList().put(distance, this.getNumber());

            if (distance <= 200) {
                inCloseRange = true;
                nextIncident.setInCloseRange(true);
                closeIds.add(rangeId + String.format(" (%.2f м)", distance));
                nextIncident.getCloseIds().add(rangeId + String.format(" (%.2f м)", distance));
                rangeId = rangeId + 1;

            } else {
                inMediumRange = true;
                nextIncident.setInMediumRange(true);
                mediumIds.add(rangeId + String.format(" (%.2f м)", distance));
                nextIncident.getMediumIds().add(rangeId + String.format(" (%.2f м)", distance));
                rangeId = rangeId + 1;
            }
        }
        return rangeId;

        //        System.out.printf("%s, %s, %s%n", distance, this.isClose(), this.isFar());

    }

}
