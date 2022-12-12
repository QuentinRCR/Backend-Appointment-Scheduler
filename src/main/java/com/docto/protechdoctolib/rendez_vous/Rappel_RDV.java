package com.docto.protechdoctolib.rendez_vous;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.Query;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

public class Rappel_RDV {


    @Configuration
    @EnableScheduling
    public class RappelRDV {
        private Rendez_vousDAO rendez_vousDAO;

        @Scheduled(fixedRate = 10000)// Every 24 hours
        public void rappel(){

            Object List_RDv = rendez_vousDAO.rappel();
            System.out.println(List_RDv);
        };
    }
}
