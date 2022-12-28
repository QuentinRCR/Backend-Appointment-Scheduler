package com.docto.protechdoctolib.rendez_vous;

import com.docto.protechdoctolib.user.User;
import com.docto.protechdoctolib.user.UserRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Permet d'exporter des informations sous Excel
 */
public class Export_excel {

    /**
     * Exporte tous les rendez-vous entre startDate et endDate sous format excel et crée le document dans les recourses de l'application
     * @param rendez_vousDAO
     * @param userDao
     * @param startDate
     * @param endDate
     * @return Le chemin d'accès absolu vers le document qui a été généré
     * @throws IOException
     */
    public static String exportAppointements(Rendez_vousDAO rendez_vousDAO, UserRepository userDao, LocalDate startDate,LocalDate endDate) throws IOException {
        //get the list of appointements between startDate and endDate
        List<Rendez_vous> rdv_list = rendez_vousDAO.export(startDate.atStartOfDay(),endDate.plusDays(1).atStartOfDay()); //get the beginning of the day to get all appointments of the day and have a LocalDateTime type

        //Create the Excel document
        String absolutePath=new File("").getAbsolutePath()+"/src/main/resources/DetailRdv.xlsx";
        OutputStream fileOut = new FileOutputStream(absolutePath);
        Workbook wb = new XSSFWorkbook();
        Sheet sh = wb.createSheet();
        Row headerRow = sh.createRow(0);

        CreationHelper createHelper = wb.getCreationHelper();

        //Create styles for the cells
        CellStyle cellStyle = wb.createCellStyle();
        cellStyle.setDataFormat((short)20); //set the format of this column to hours
        cellStyle.setAlignment(CellStyle.ALIGN_CENTER);

        CellStyle cellStyle2 = wb.createCellStyle();
        cellStyle2.setDataFormat((short)31);
        cellStyle2.setAlignment(CellStyle.ALIGN_CENTER);

        CellStyle cellStyle3 = wb.createCellStyle();
        cellStyle3.setAlignment(CellStyle.ALIGN_CENTER);

        //Create the header of the document
        String[] row_heading = {"Semaine", "Date", "Heure", "Patient", "Mode de règlement", "Commentaires","Payé","Relances"};
        // Creating Header
        for (int i=0; i< row_heading.length; i++){
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(row_heading[i]);
        }



        WeekFields weekFields = WeekFields.of(Locale.getDefault()); //variable to get the week number
        User currentUser;
        Cell cell;

        //Fill with datas with one appointment per line
        for (int i=0; i<rdv_list.size(); i++){
            currentUser = userDao.getReferenceById(rdv_list.get(i).getIdUser()); //get the user

            Row dataRow = sh.createRow(i+1); //Create a row
            cell=dataRow.createCell(0); //create the first cell
            cell.setCellValue(rdv_list.get(i).getDateDebut().toLocalDate().get(weekFields.weekOfWeekBasedYear())); //get the week number of the date
            cell =dataRow.createCell(1);
            cell.setCellStyle(cellStyle2);
            cell.setCellValue(Date.from(rdv_list.get(i).getDateDebut().atZone(ZoneId.systemDefault()).toInstant())); //set the date as timestamp for Excel to understand
            cell= dataRow.createCell(2);
            cell.setCellValue(Date.from(rdv_list.get(i).getDateDebut().atZone(ZoneId.systemDefault()).toInstant()));//set the time as timestamp for Excel to understand
            cell.setCellStyle(cellStyle);
            cell= dataRow.createCell(3);
            cell.setCellValue(currentUser.getNom()+" "+currentUser.getPrenom()); //Set client infos
            cell.setCellStyle(cellStyle3);
            cell=dataRow.createCell(4);
            cell.setCellValue("EMSE");
            cell.setCellStyle(cellStyle3);
        }


        //Ajuste the width of colums to feet the content
        for (int i=0; i< row_heading.length; i++){
            sh.autoSizeColumn(i);
        }



        // Write data to excel
        try {
            wb.write(fileOut);
            fileOut.close();
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }

        return absolutePath;
    }


}

