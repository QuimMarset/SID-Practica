package Ontologia;

import jade.content.AgentAction;

public class ResultadoPropuesta implements AgentAction {

    private float volumen;
    private float coste;


    public void setVolumen(float volumen) { this.volumen = volumen; }
    public void setCoste(float coste) { this.coste = coste; }

    public float getVolumen() { return volumen; }
    public float getCoste() { return coste; }
}