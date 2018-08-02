package Ontologia;

import jade.content.AgentAction;

public class Propuesta implements AgentAction {

    private MasaDeAgua masaDeAgua;
    private float presupuesto;

    public void setMasaDeAgua(MasaDeAgua masaDeAgua) { this.masaDeAgua = masaDeAgua; }
    public void setPresupuesto(float presupuesto) { this.presupuesto = presupuesto; }

    public MasaDeAgua getMasaDeAgua() { return masaDeAgua; }
    public float getPresupuesto() { return presupuesto; }
}