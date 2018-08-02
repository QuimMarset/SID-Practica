package Ontologia;

import jade.content.AgentAction;

public class SolicitarVertido implements AgentAction {

    private MasaDeAgua masaDeAgua;

    public void setMasaDeAgua(MasaDeAgua masaDeAgua) { this.masaDeAgua = masaDeAgua; }

    public MasaDeAgua getMasaDeAgua() { return masaDeAgua; }
}