package Agentes;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.SimpleAchieveREResponder;
import java.util.ArrayList;
import java.util.List;
import Ontologia.*;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;


public class Rio extends Agent implements Vocabulary {
    private List<MasaDeAgua> tramos = new ArrayList <> ();
    private int tramoActual;
    private int numTramos;
    private float volumenTramos = 100000;
    private Codec codec = new SLCodec();
    private Ontology ontology = OntologyMgr.getInstance();
    
    public Rio() {
        super();
        numTramos = 10; 
        MasaDeAgua masaBase;
        for (int i = 0;i < numTramos; ++i) {
            masaBase = new MasaDeAgua(0.75f, 2.25f, 30, 20, 50, volumenTramos);
            tramos.add(masaBase);
        }
        tramoActual = 0;
    }

    private class FluirRio extends TickerBehaviour {

        FluirRio(Agent a, long period) {
            super(a, period);
        }

        protected void onTick() {
            System.out.println("El río fluye y el tramo actual cambia");
            tramoActual = (tramoActual + 1) % numTramos;
        }
    }
    
    private class GestionarMensajesRecibidosBehaviour extends SimpleAchieveREResponder {

        private float volumenAExtraer;
        private boolean esExtraccionAgua;
        
        GestionarMensajesRecibidosBehaviour(Agent a, MessageTemplate mt) {
            super(a, mt);
        }
        
        protected ACLMessage prepareResponse(ACLMessage request) {
            ACLMessage reply = null;
            try {
                Action accion = (Action) getContentManager().extractContent(request);
                if (accion.getAction() instanceof ExtraerAgua) {
                    esExtraccionAgua = true;
                    reply = request.createReply();
                    ExtraerAgua extraccion = (ExtraerAgua) accion.getAction();
                    volumenAExtraer = extraccion.getVolumen();
                    float volumenTramo = tramos.get(tramoActual).getVolumen();
                    if (volumenAExtraer >= volumenTramo) {
                        reply.setPerformative(ACLMessage.REFUSE);
                        reply.setContent("Petición de extracción rechazada. Tramo de río vacío");
                    }
                    else {
                        reply.setPerformative(ACLMessage.AGREE);
                        reply.setContent("Petición de extracción aceptada");
                    }
                }
                else if (accion.getAction() instanceof VertirAgua) {
                    esExtraccionAgua = false;
                    System.out.println(getLocalName() + ": ha recibido el vertido de " + request.getSender().getLocalName());
                    VertirAgua vertido = (VertirAgua) accion.getAction();
                    MasaDeAgua aguaVertido = vertido.getMasaDeAgua();
                    
                }
                else if (accion.getAction() instanceof EnviarLluvia) {
                    esExtraccionAgua = false;
                    System.out.println(getLocalName() + ": ha recibido agua de la lluvia");
                    EnviarLluvia envioLluvia = (EnviarLluvia) accion.getAction();
                    MasaDeAgua aguaLluvia = envioLluvia.getMasaDeAgua();
                    for (int i = 0; i < numTramos; ++i) {
                        tramos.get(i).mezclarAguas(aguaLluvia);
                    }
                }
            }
            catch(Exception ex) { ex.printStackTrace(); }
            return reply;
        }
        
        protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) {
            ACLMessage inform = null;
            if (esExtraccionAgua) {
                inform = request.createReply();
                inform.setPerformative(ACLMessage.INFORM);
                float volumenTramo = tramos.get(tramoActual).getVolumen();
                MasaDeAgua masaExtraida = new MasaDeAgua(tramos.get(tramoActual));
                masaExtraida.setVolumen(volumenAExtraer);
                tramos.get(tramoActual).setVolumen(volumenTramo - volumenAExtraer);
                Action accion = new Action(request.getSender(), masaExtraida);
                try {
                    getContentManager().fillContent(inform, accion);
                }
                catch (Exception ex) { ex.printStackTrace(); }
            }
            return inform;
        }
    }
    
    protected void setup() {

        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(ontology);

        MessageTemplate mt = MessageTemplate.or(
            MessageTemplate.and(
                MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST),
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST)
            ),
            MessageTemplate.MatchPerformative(ACLMessage.INFORM));
        GestionarMensajesRecibidosBehaviour b = new GestionarMensajesRecibidosBehaviour(this, mt);
        addBehaviour(b);
        FluirRio b2 = new FluirRio(this, 20000); 
        addBehaviour(b2);
    }
}
