import model.Incident;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.text.html.parser.Entity;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class RICalc {

    private static final double EARTH_RADIUS = 6378137;

    public static void main(String[] args) {

        FileInputStream file = null;
        XSSFWorkbook workbook = null;

        try {
            file = new FileInputStream(new File("src/main/resources/ПеШ2 и более.xlsx"));
            workbook = new XSSFWorkbook(file);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        XSSFSheet sheet = workbook.getSheetAt(0);

        List<Incident> incidents = new ArrayList<>();


        Iterator<Row> iterator = sheet.iterator();
        if (iterator.hasNext()) {
            iterator.next();
        }

        while (iterator.hasNext()) {
            Incident incident = new Incident();
            Row row = iterator.next();
            if (Objects.isNull(row.getCell(0, Row.MissingCellPolicy.RETURN_NULL_AND_BLANK))) {
                break;
            }
            incident.fillIncident(row);
            incidents.add(incident);
        }


        Collections.sort(incidents);

        List<Incident> allowedIncidents = new ArrayList<>();
        allowedIncidents.add(incidents.get(0));

        double minDiff = 0.008983D;


        Integer rangeId = 1;

        for (int i = 1; i < incidents.size(); i++) {
            Incident currentIncident = incidents.get(i);
//            while (true) {
//                if (!allowedIncidents.isEmpty() && currentIncident.getLatitude() - allowedIncidents.get(0).getLatitude() > minDiff) {
//                    allowedIncidents.remove(0);
//                } else {
//                    break;
//                }
//            }

            for (Incident allowedIncident : allowedIncidents) {
                rangeId = currentIncident.calculateDistance(allowedIncident, rangeId);
            }
            allowedIncidents.add(currentIncident);
        }


//        XSSFWorkbook workbookClose = new XSSFWorkbook();
//        XSSFWorkbook workbookMedium = new XSSFWorkbook();
        XSSFWorkbook workbookAll = new XSSFWorkbook();


//        XSSFSheet sheetClose = workbookClose.createSheet("Calculate Close Range");
//        XSSFSheet sheetMedium = workbookMedium.createSheet("Calculate Medium Range");
        XSSFSheet sheetAll = workbookAll.createSheet("Calculate All Range");

//        fillHeader(sheetClose);
//        fillHeader(sheetMedium);
        fillHeader(sheetAll);

//        int closeRowCounter = 1;
//        int mediumRowCounter = 1;
        int allRowCounter = 1;

        for (Incident incident : incidents) {

//            if (incident.isInRange()) {
            if (true) {


//                for (Map.Entry<Double, Double> distanceInfo : incident.getDistanceList().entrySet()) {
//
//                    Row row = sheetAll.createRow(allRowCounter);
//                    allRowCounter++;
//
//                    incident.fillRow(row, distanceInfo);
//                }
                Row row = sheetAll.createRow(allRowCounter);
                allRowCounter++;

                incident.fillRow(row);
            }


//            if(incident.isInCloseRange()){
//                Row closeRow = sheetClose.createRow(closeRowCounter);
//                closeRowCounter++;
//                incident.fillRow(closeRow);
//
//                Row allRow = sheetAll.createRow(allRowCounter);
//                allRowCounter++;
//                incident.fillRow(allRow);
//            }
//
//            if(incident.isInMediumRange() && !incident.isInCloseRange()){
//
//                Row mediumRow = sheetMedium.createRow(mediumRowCounter);
//                mediumRowCounter++;
//                incident.fillRow(mediumRow);
//
//                Row allRow = sheetAll.createRow(allRowCounter);
//                allRowCounter++;
//                incident.fillRow(allRow);

        }


//        try {
//            FileOutputStream out =  new FileOutputStream(new File("0-200 2.xlsx"));
//            workbookClose.write(out);
//            out.close();
//            System.out.println("Excel for close range written successfully");
//
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        try {
//            FileOutputStream out =  new FileOutputStream(new File("200-1000.xlsx"));
//            workbookMedium.write(out);
//            out.close();
//            System.out.println("Excel for medium range written successfully");
//
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        try {
            FileOutputStream out = new FileOutputStream(new File("0-1000.xlsx"));
            workbookAll.write(out);
            out.close();
            System.out.println("Excel for all range written successfully");

        } catch (
                FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private static void fillHeader(XSSFSheet sheet) {
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("data");
        header.createCell(1).setCellValue("number");
        header.createCell(2).setCellValue("type dtp");
        header.createCell(3).setCellValue("died");
        header.createCell(4).setCellValue("wounded");
        header.createCell(5).setCellValue("district");
        header.createCell(6).setCellValue("region");
        header.createCell(7).setCellValue("locality");
        header.createCell(8).setCellValue("value road");
        header.createCell(9).setCellValue("road");
        header.createCell(10).setCellValue("km");
        header.createCell(11).setCellValue("street");
        header.createCell(12).setCellValue("home");
        header.createCell(13).setCellValue("SH");
        header.createCell(14).setCellValue("D");
        header.createCell(16).setCellValue("x<200");
        header.createCell(17).setCellValue("200<x<1000");
    }

    private static void calculateDistance(Incident currentIncident, Incident nextIncident) {
        Double radLatMain = toRadian(currentIncident.getLatitude());
        Double radLatNext = toRadian(nextIncident.getLatitude());

        double latDiff = radLatMain - radLatNext;
        double lonDiff = toRadian(currentIncident.getLatitude()) - toRadian(nextIncident.getLongitude());
        Double distance = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(latDiff / 2), 2)
                + Math.cos(radLatMain) * Math.cos(radLatNext) * Math.pow(Math.sin(lonDiff / 2), 2)));
        distance = distance * EARTH_RADIUS;


        if(distance <= 1000){

            currentIncident.setInRange(true);
            nextIncident.setInRange(true);

            currentIncident.getDistanceList().put(distance, nextIncident.getNumber());
            nextIncident.getDistanceList().put(distance, currentIncident.getNumber());


//            if(distance <= 200){
//                inCloseRange = true;
//                nextIncident.setInCloseRange(true);
//            }
//            else {
//                inMediumRange = true;
//                nextIncident.setInMediumRange(true);
//            }
        }

//        System.out.printf("%s, %s, %s%n", distance, this.isClose(), this.isFar());

    }

    private static Double toRadian(Double coordinate) {
        return coordinate * Math.PI / 180.0;
    }


}
