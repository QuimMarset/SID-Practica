
package Ontologia;

import jade.content.Concept;
import java.util.Random;

public class MasaDeAgua implements Concept {

    private float DBO;
    private float DQO;
    private float Solidos_suspension;
    private float Total_nitratos;
    private float Total_sulfatos;
    private float Volumen;
    
    public MasaDeAgua() {
        DBO = 0;
        DQO = 0;
        Solidos_suspension = 0;
        Total_nitratos = 0;
        Total_sulfatos = 0;
        Volumen = 0;
    }
    
    public MasaDeAgua(MasaDeAgua masa) {
        DBO = masa.getDBO();
        DQO = masa.getDQO();
        Solidos_suspension = masa.getSolidos_suspension();
        Total_nitratos = masa.getTotal_nitratos();
        Total_sulfatos = masa.getTotal_sulfatos();
        Volumen = masa.getVolumen();
        
    }

    public MasaDeAgua(float DBO, float DQO, float Solidos,
                      float Nitratos, float Sulfatos, float Volumen) {
        this.DBO = DBO;
        this.DQO = DQO;
        Solidos_suspension = Solidos;
        Total_nitratos = Nitratos;
        Total_sulfatos = Sulfatos;
        this.Volumen = Volumen;
    }
    
    public void mezclarAguas(MasaDeAgua masa) {
        float VolumenFinal = Volumen + masa.getVolumen();
        DBO = (masa.getDBO() * masa.getVolumen() + DBO*Volumen)  / VolumenFinal;
        DQO = (masa.getDQO() * masa.getVolumen() + DQO * Volumen) / VolumenFinal;
        Solidos_suspension = (masa.getSolidos_suspension() * masa.getVolumen() + Solidos_suspension * Volumen) / VolumenFinal;
        Total_nitratos = (masa.getTotal_nitratos() * masa.getVolumen() + Total_nitratos * Volumen) / VolumenFinal;
        Total_sulfatos = (masa.getTotal_sulfatos() * masa.getVolumen() + Total_sulfatos * Volumen) / VolumenFinal;
        Volumen = VolumenFinal;
    }
    
    public void ensuciarAguas() {
        DBO = DBO + 1;
        DQO = DQO + 2.5f;
        Solidos_suspension = Solidos_suspension + 30;
        Total_nitratos = Total_nitratos + 25 ;
        Total_sulfatos = Total_sulfatos + 20;
    }
    
    public void limpiarAguas() {
        Random random = new Random();
        DBO = random.nextInt((int) (1.5 - 0.75 + 1));
        DQO = random.nextInt((5 - 1 + 1)) + 1;
        Solidos_suspension = random.nextInt((50 - 25 + 1)) + 25;
        Total_nitratos =  random.nextInt((50 - 10 + 1)) + 10;
        Total_sulfatos = random.nextInt((400 - 50 + 1)) + 50;;
    }
    
    public boolean muyContaminada() {
        int cont = 0;
        if (DBO >= 500) cont = cont + 1;
        if (DQO >= 1000) cont = cont + 1;
        if (Solidos_suspension >= 500) cont = cont + 1;
        if (Total_nitratos >= 400 ) cont = cont + 1;
        if (Total_sulfatos >= 500) cont = cont + 1;
        return (cont >= 3);
    }

    public void setDBO(float DBO) { this.DBO = DBO; }
    public void setDQO(float DQO) { this.DQO = DQO; }
    public void setSolidos_suspension(float Solidos_suspension) { this.Solidos_suspension = Solidos_suspension; }
    public void setTotal_nitratos(float Total_nitratos) { this.Total_nitratos = Total_nitratos; }
    public void setTotal_sulfatos(float Total_sulfatos) { this.Total_sulfatos = Total_sulfatos; }
    public void setVolumen(float Volumen) { this.Volumen = Volumen; }

    public float getDBO() {
        return DBO;
    }
    public float getDQO() {
        return DQO;
    }
    public float getSolidos_suspension() {
        return Solidos_suspension;
    }
    public float getTotal_nitratos() {
        return Total_nitratos;
    }
    public float getTotal_sulfatos() {
        return Total_sulfatos;
    }
    public float getVolumen() { return Volumen; }

}