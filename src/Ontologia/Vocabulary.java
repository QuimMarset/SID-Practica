/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Ontologia;

/**
 *
 * @author marta
 */
public interface Vocabulary {
    
    // Conceptos
    public static final String MASA_AGUA = "MasaDeAgua";
    public static final String MASA_AGUA_DBO = "DBO";
    public static final String MASA_AGUA_DQO = "DQO";
    public static final String MASA_AGUA_SOLIDOS_SUSPENSION = "Solidos_suspension";
    public static final String MASA_AGUA_TOTAL_NITRATOS = "Total_nitratos";
    public static final String MASA_AGUA_TOTAL_SULFATOS = "Total_sulfatos";
    public static final String MASA_AGUA_VOLUMEN = "Volumen";
    
    // AgentActions
    
    public static final String PROPUESTA = "Propuesta";
    public static final String PROPUESTA_MASA_AGUA = "masaDeAgua";
    public static final String PROPUESA_PRESUPUESTO = "presupuesto";
    
    public static final String RESULTADO_PROPUESTA = "ResultadoPropuesta";
    public static final String RESULTADO_PROPUESTA_VOLUMEN = "volumen";
    public static final String RESULTADO_PROPUESTA_COSTE = "coste";
    
    public static final String VERTIR_AGUA = "VertirAgua";
    public static final String VERTIR_AGUA_MASA_AGUA = "masaDeAgua";    
    
    public static final String GESTIONAR_COSTE_DEPURACION = "GestionarCosteDepuracion";
    public static final String GESTIONAR_COSTE_DEPURACION_COSTE = "coste";
    
    public static final String EXTRAER_AGUA = "ExtraerAgua";
    public static final String EXTRAER_AGUA_VOLUMEN = "volumen";

    public static final String ENVIAR_LLUVIA = "EnviarLluvia";
    public static final String ENVIAR_LLUVIA_MASA_AGUA = "masaDeAgua";

    public static final String SOLICITAR_VERTIDO = "SolicitarVertido";
    public static final String SOLICITAR_VERTIDO_MASA_AGUA = "masaDeAgua";
    
    
}
