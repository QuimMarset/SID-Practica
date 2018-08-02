/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Ontologia;
import jade.content.onto.*;
import jade.content.schema.*;

public class OntologyMgr extends Ontology implements Vocabulary {
     // ----------> The name identifying this ontology
   public static final String ONTOLOGY_NAME = "Ontologia";

   // ----------> The singleton instance of this ontology
   private static Ontology instance = new OntologyMgr();

   // ----------> Method to access the singleton ontology object
   public static Ontology getInstance() { return instance; }


   // Private constructor
   private OntologyMgr() {

      super(ONTOLOGY_NAME, BasicOntology.getInstance());

      try {

         // Concepts

         // MasaDeAgua
         ConceptSchema cs = new ConceptSchema(MASA_AGUA);
         add(cs, MasaDeAgua.class);
         cs.add(MASA_AGUA_DBO, (PrimitiveSchema) getSchema(BasicOntology.FLOAT), ObjectSchema.MANDATORY);
         cs.add(MASA_AGUA_DQO, (PrimitiveSchema) getSchema(BasicOntology.FLOAT), ObjectSchema.MANDATORY);
         cs.add(MASA_AGUA_SOLIDOS_SUSPENSION, (PrimitiveSchema) getSchema(BasicOntology.FLOAT), ObjectSchema.MANDATORY);
         cs.add(MASA_AGUA_TOTAL_NITRATOS, (PrimitiveSchema) getSchema(BasicOntology.FLOAT), ObjectSchema.MANDATORY);
         cs.add(MASA_AGUA_TOTAL_SULFATOS, (PrimitiveSchema) getSchema(BasicOntology.FLOAT), ObjectSchema.MANDATORY);
         cs.add(MASA_AGUA_VOLUMEN, (PrimitiveSchema) getSchema(BasicOntology.FLOAT), ObjectSchema.MANDATORY);

         // AgentActions

         //  Propuesta
         AgentActionSchema as = new AgentActionSchema(PROPUESTA);
         add(as, Propuesta.class);
         as.add(PROPUESTA_MASA_AGUA, (ConceptSchema) getSchema(MASA_AGUA), ObjectSchema.MANDATORY);
         as.add(PROPUESA_PRESUPUESTO, (PrimitiveSchema) getSchema(BasicOntology.FLOAT), ObjectSchema.OPTIONAL);
         
         //  Resultado Propuesta
         as = new AgentActionSchema(RESULTADO_PROPUESTA);
         add(as, ResultadoPropuesta.class);
         as.add(RESULTADO_PROPUESTA_VOLUMEN, (PrimitiveSchema) getSchema(BasicOntology.FLOAT), ObjectSchema.MANDATORY);
         as.add(RESULTADO_PROPUESTA_COSTE, (PrimitiveSchema) getSchema(BasicOntology.FLOAT), ObjectSchema.MANDATORY);
         
         //  Vertir Agua
         as = new AgentActionSchema(VERTIR_AGUA);
         add(as, VertirAgua.class);
         as.add(VERTIR_AGUA_MASA_AGUA, (ConceptSchema) getSchema(MASA_AGUA), ObjectSchema.MANDATORY);
         
         //  Extraer agua
         as = new AgentActionSchema(EXTRAER_AGUA);
         add(as, ExtraerAgua.class);
         as.add(EXTRAER_AGUA_VOLUMEN, (PrimitiveSchema) getSchema(BasicOntology.FLOAT), ObjectSchema.MANDATORY);

         // Enviar Lluvia
         as = new AgentActionSchema(ENVIAR_LLUVIA);
         add(as, EnviarLluvia.class);
         as.add(ENVIAR_LLUVIA_MASA_AGUA, (ConceptSchema) getSchema(MASA_AGUA), ObjectSchema.MANDATORY);

         // Solicitar Vertido
         as = new AgentActionSchema(SOLICITAR_VERTIDO);
         add(as, SolicitarVertido.class);
         as.add(SOLICITAR_VERTIDO_MASA_AGUA, (ConceptSchema) getSchema(MASA_AGUA), ObjectSchema.MANDATORY);

      }
      catch (OntologyException oe) {
         oe.printStackTrace();
      }
   }
}
