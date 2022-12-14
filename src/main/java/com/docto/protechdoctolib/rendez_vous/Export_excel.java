package com.docto.protechdoctolib.rendez_vous;

import java.util.List;

public class Export_excel {

    private Rendez_vousDAO rendez_vousDAO;

    public  Export_excel(Rendez_vousDAO rendez_vousDAO){
        this.rendez_vousDAO = rendez_vousDAO;
    }

    public void export(){
        List<Rendez_vous> rdv_list = rendez_vousDAO.export();

    }
}
