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

public class Export_excel {

    public static String export(Rendez_vousDAO rendez_vousDAO, UserRepository userDao, LocalDate startDate,LocalDate endDate) throws IOException {
        List<Rendez_vous> rdv_list = rendez_vousDAO.export(startDate.atStartOfDay(),endDate.plusDays(1).atStartOfDay()); //get the beginning of the day to get all appointments of the day

        String absolutePath=new File("").getAbsolutePath()+"/src/main/resources/DetailRdv.xlsx";
        OutputStream fileOut = new FileOutputStream(absolutePath);
        Workbook wb = new XSSFWorkbook();
        Sheet sh = wb.createSheet();
        sh.autoSizeColumn(1);
        Row headerRow = sh.createRow(0);

        CreationHelper createHelper = wb.getCreationHelper();

        CellStyle cellStyle = wb.createCellStyle();
        cellStyle.setDataFormat((short)20); //set the format of this column to hours
        cellStyle.setAlignment(CellStyle.ALIGN_CENTER);

        CellStyle cellStyle2 = wb.createCellStyle();
        cellStyle2.setDataFormat((short)31);
        cellStyle2.setAlignment(CellStyle.ALIGN_CENTER);

        CellStyle cellStyle3 = wb.createCellStyle();
        cellStyle3.setAlignment(CellStyle.ALIGN_CENTER);

        String[] row_heading = {"Semaine", "Date", "Heure", "Patient", "Mode de règlement", "Commentaires","Payé","Relances"};
        // Creating Header
        for (int i=0; i< row_heading.length; i++){
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(row_heading[i]);
        }



        // Creating data rows for each appointment
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        User currentUser;

        ZoneId defaultZoneId = ZoneId.systemDefault();
        Cell cell;

        for (int i=0; i<rdv_list.size(); i++){
            currentUser = userDao.getReferenceById(rdv_list.get(i).getIdUser());
            Row dataRow = sh.createRow(i+1);
            cell=dataRow.createCell(0); //get the week number of the date
            cell.setCellValue(rdv_list.get(i).getDateDebut().toLocalDate().get(weekFields.weekOfWeekBasedYear()));
            cell =dataRow.createCell(1);
            cell.setCellStyle(cellStyle2);
            cell.setCellValue(Date.from(rdv_list.get(i).getDateDebut().atZone(ZoneId.systemDefault()).toInstant()));
            cell= dataRow.createCell(2);
            cell.setCellValue(Date.from(rdv_list.get(i).getDateDebut().atZone(ZoneId.systemDefault()).toInstant()));
            cell.setCellStyle(cellStyle);
            cell= dataRow.createCell(3);
            cell.setCellValue(currentUser.getNom()+" "+currentUser.getPrenom());
            cell.setCellStyle(cellStyle3);
            cell=dataRow.createCell(4);
            cell.setCellValue("EMSE");
            cell.setCellStyle(cellStyle3);
        }



        for (int i=0; i< row_heading.length; i++){ //set the size of columns
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

