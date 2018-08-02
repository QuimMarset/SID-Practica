package Agentes;

import Ontologia.*;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetInitiator;
import jade.proto.SimpleAchieveREResponder;

import java.util.*;


public class EDAR extends Agent {

    private float capacidad;
    private MasaDeAgua aguaTanque;
    private List<AID> industriasAID;
    private AID rioAID;
    private AID lluviaAID;
    private Codec codec = new SLCodec();
    private Ontology ontology = OntologyMgr.getInstance();
    
    public EDAR() {
        industriasAID = new ArrayList <> ();
        aguaTanque = new MasaDeAgua();
        capacidad = 10000;
    }

    private class RepetirProcesoDepuracion extends TickerBehaviour {

        private class ContractNetInitiatorBehaviour extends ContractNetInitiator {

            private Random random;

            private float calcularCosteDepuracion(MasaDeAgua masa) {
                float coste = 0;
                float volumen = masa.getVolumen();
                float coste_DBO = 0.85f;
                float coste_DQO = 0.75f;
                float coste_Solidos_suspension = 0.24f;
                float coste_Total_nitratos = 0.15f;
                float coste_Total_sulfatos = 0.2f;
                if (masa.getDBO() > 1.5) coste = coste + masa.getDBO()*volumen*coste_DBO;
                if (masa.getDQO() > 5) coste = coste + masa.getDQO()*volumen*coste_DQO;
                if (masa.getSolidos_suspension() > 50) coste = coste + masa.getSolidos_suspension()*volumen*coste_Solidos_suspension;
                if (masa.getTotal_nitratos() > 50) coste = coste + masa.getTotal_nitratos()*volumen*coste_Total_nitratos;
                if (masa.getTotal_sulfatos() > 400) coste = coste + masa.getTotal_sulfatos()*volumen*coste_Total_sulfatos;
                return coste;
            }

            ContractNetInitiatorBehaviour(Agent a, ACLMessage msg) {
                super(a, msg);
                random = new Random();
            }

            protected  void handleAllResponses(Vector responses, Vector acceptances) {
                Enumeration e = responses.elements();
                SortedMap <Float, ACLMessage> sortedMap = new TreeMap <> ();
                while (e.hasMoreElements()) {
                    ACLMessage msg_i = (ACLMessage) e.nextElement();
                    try {
                        if (msg_i.getContent() != null) {
                            Action accion = (Action) getContentManager().extractContent(msg_i);
                            Propuesta propuestaVertido = (Propuesta) accion.getAction();
                            MasaDeAgua masaVertido = propuestaVertido.getMasaDeAgua();
                            float presupuesto = propuestaVertido.getPresupuesto();
                            sortedMap.put(masaVertido.getVolumen()/presupuesto, msg_i);
                        }
                    }
                    catch (Exception ex) { ex.printStackTrace();}
                }
                float volumen = aguaTanque.getVolumen();
                for (SortedMap.Entry <Float, ACLMessage> entry : sortedMap.entrySet()) {
                    try {
                        ACLMessage msg_i = entry.getValue();
                        Action accion = (Action) getContentManager().extractContent(msg_i);
                        Propuesta propuestaVertido = (Propuesta) accion.getAction();
                        MasaDeAgua masaVertido = propuestaVertido.getMasaDeAgua();
                        ACLMessage reply = msg_i.createReply();
                        if (volumen < capacidad) {
                            float volumenRestante = capacidad - volumen;
                            float volumenPropuesto = masaVertido.getVolumen();
                            float volumenRespuesta;
                            if (volumenPropuesto >= volumenRestante) {
                                float aux = volumenRestante*0.1f;
                                volumenRespuesta = (float) (random.nextFloat() *
                                        (volumenRestante - aux) + aux);
                            }
                            else {
                                float aux = volumenPropuesto*0.1f;
                                volumenRespuesta = (float) (random.nextFloat() *
                                        (volumenRestante - aux) + aux);
                            }
                            volumen = volumen + volumenPropuesto;
                            reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                            System.out.println(getLocalName() +
                                    ": Acepto a " + msg_i.getSender().getLocalName());
                            ResultadoPropuesta resultado = new ResultadoPropuesta();
                            resultado.setVolumen(volumenRespuesta);
                            masaVertido.setVolumen(volumenRespuesta);
                            float coste = calcularCosteDepuracion(masaVertido);
                            resultado.setCoste(coste);
                            accion = new Action(msg_i.getSender(), resultado);
                            getContentManager().fillContent(reply, accion);
                        }
                        else {
                            reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                            System.out.println(getLocalName() +
                                    ": Rechazo a " + msg_i.getSender().getName());
                        }
                        acceptances.addElement(reply);
                    }
                    catch (Exception ex) { ex.printStackTrace();}
                }
            }

            protected void handleInform(ACLMessage inform) {
                String sender = inform.getSender().getLocalName();
                System.out.println(getLocalName() +
                        ": agente " + sender + " ha informado de la aceptación");
                try {
                    Action accion = (Action) getContentManager().extractContent(inform);
                    Propuesta propuesta = (Propuesta) accion.getAction();
                    MasaDeAgua masaEnviada = propuesta.getMasaDeAgua();
                    aguaTanque.mezclarAguas(masaEnviada);
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        }

        RepetirProcesoDepuracion(Agent a, long period) {
            super(a, period);
        }

        protected void onTick() {
            float porcentageLleno = aguaTanque.getVolumen()/capacidad;
            if (porcentageLleno >= 0.25 && porcentageLleno <= 0.5) {
                System.out.println(getLocalName() +": Inicio contractNet");
                ACLMessage cfpMessage = new ACLMessage(ACLMessage.CFP);
                cfpMessage.setSender(getAID());
                cfpMessage.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
                cfpMessage.setOntology(ontology.getName());
                cfpMessage.setLanguage(codec.getName());
                for (AID industriasAID_i : industriasAID) {
                    cfpMessage.addReceiver(industriasAID_i);
                }
                ContractNetInitiatorBehaviour cnib = new ContractNetInitiatorBehaviour(myAgent, cfpMessage);
                addBehaviour(cnib);
            }
            else if (porcentageLleno >= 0.75) {
                ACLMessage msgRio = new ACLMessage(ACLMessage.INFORM);
                msgRio.setSender(getAID());
                msgRio.addReceiver(rioAID);
                msgRio.setOntology(ontology.getName());
                msgRio.setLanguage(codec.getName());
                aguaTanque.limpiarAguas();
                MasaDeAgua aguaEdar = new MasaDeAgua(aguaTanque);
                VertirAgua vertir = new VertirAgua();
                vertir.setMasaDeAgua(aguaEdar);
                Action act = new Action(rioAID, vertir);
                try {
                    myAgent.getContentManager().fillContent(msgRio, act);
                }
                catch (Exception ex) { ex.printStackTrace();}
                aguaTanque = new MasaDeAgua();
                System.out.println(getLocalName() + ": Realiza vertido al rio");
                send(msgRio);
            }
        }
    }

    private class GestionarVertidoBehaviour extends SimpleAchieveREResponder {

        GestionarVertidoBehaviour(Agent a, MessageTemplate mt) {
            super(a, mt);
        }

        private boolean aceptaSolicitud(MasaDeAgua masa) {
            float volumenTanque = aguaTanque.getVolumen();
            return capacidad - volumenTanque >= masa.getVolumen() && !masa.muyContaminada();
        }

        protected ACLMessage prepareResponse(ACLMessage request) {
            ACLMessage reply = null;
            try {
                Action accion = (Action) getContentManager().extractContent(request);
                if (accion.getAction() instanceof SolicitarVertido) {
                    SolicitarVertido solicitudVertido = (SolicitarVertido) accion.getAction();
                    MasaDeAgua masaVertido = solicitudVertido.getMasaDeAgua();
                    reply = request.createReply();
                    if (aceptaSolicitud(masaVertido)) {
                        reply.setPerformative(ACLMessage.AGREE);
                        aguaTanque.mezclarAguas(masaVertido);
                    }
                    else {
                        System.out.println(getLocalName() +": NO acepta vertido");
                        reply.setPerformative(ACLMessage.REFUSE);
                    }
                }
                else if (accion.getAction() instanceof EnviarLluvia) {
                    EnviarLluvia envioLluvia = (EnviarLluvia) accion.getAction();
                    MasaDeAgua aguaLluvia = envioLluvia.getMasaDeAgua();
                    System.out.println(getLocalName() + ": recibe agua de la lluvia");
                    aguaLluvia.setVolumen(Math.min(capacidad - aguaTanque.getVolumen(),
                            aguaLluvia.getVolumen()));
                    aguaTanque.mezclarAguas(aguaLluvia);
                }
            }
            catch(Exception ex) { ex.printStackTrace(); }
            return reply;  
        }

        protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) {
            ACLMessage inform = request.createReply();
            inform.setPerformative(ACLMessage.INFORM);
            try {
                Action accion = (Action) getContentManager().extractContent(request);
                if (accion.getAction() instanceof SolicitarVertido) {
                    inform.setContent(getLocalName() +": Informar petición vertido realizada con éxito");
                }
                else if (accion.getAction() instanceof EnviarLluvia) {
                    inform = null;
                }
            }
            catch(Exception ex) { ex.printStackTrace(); }
            return inform;
        }
    }

    protected void setup() {
        Object [] args = getArguments();
        if (args != null) {
            String s;
            int num = args.length - 2;
            for (int i = 0; i < args.length; ++i) {
                s = args[i].toString();
                if (i < num + 1) {
                    industriasAID.add(new AID((String) args[i], AID.ISLOCALNAME));
                }
                else if (i == args.length - 1){
                    rioAID = new AID((String) args[i], AID.ISLOCALNAME);
                }
                else {
                    lluviaAID = new AID((String) args[i], AID.ISLOCALNAME);
                }
            }
        }
         // Register language and ontology
        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(ontology);
        
        RepetirProcesoDepuracion r = new RepetirProcesoDepuracion(this,15000);
        addBehaviour(r);

        MessageTemplate mt = MessageTemplate.or(
            MessageTemplate.and(
                MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST),
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST)
            ),
            MessageTemplate.and(
                MessageTemplate.MatchPerformative(ACLMessage.INFORM), 
                MessageTemplate.MatchSender(lluviaAID)));
        GestionarVertidoBehaviour b2 = new GestionarVertidoBehaviour(this, mt);
        addBehaviour(b2);

        
    }
}