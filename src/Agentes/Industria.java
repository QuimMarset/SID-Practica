/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Agentes;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.SimpleAchieveREInitiator;
import Ontologia.*;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.basic.Action;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;

import java.util.Random;


public class Industria extends Agent  {
    
    private float capacidad;
    private float presupuesto;
    private MasaDeAgua aguaTanque;
    private AID rioAID, edarAID, lluviaAID;
    private Codec codec = new SLCodec();
    private boolean estaLloviendo;
    private Ontology ontology = OntologyMgr.getInstance();

    public Industria() {
        super();
        presupuesto = 100000;
        capacidad= 10000;
        aguaTanque= new MasaDeAgua();
    }

    private float calcularCosteDepuracion() {
        float coste = 0;
        float volumen = aguaTanque.getVolumen();
        float coste_DBO = 0.85f;
        float coste_DQO = 0.75f;
        float coste_Solidos_suspension = 0.24f;
        float coste_Total_nitratos = 0.15f;
        float coste_Total_sulfatos = 0.2f;
        if (aguaTanque.getDBO() > 1.5) coste = coste + aguaTanque.getDBO()*volumen*coste_DBO;
        if (aguaTanque.getDQO() > 5) coste = coste + aguaTanque.getDQO()*volumen*coste_DQO;
        if (aguaTanque.getSolidos_suspension() > 50) coste = coste + aguaTanque.getSolidos_suspension()*volumen*coste_Solidos_suspension;
        if (aguaTanque.getTotal_nitratos() > 50) coste = coste + aguaTanque.getTotal_nitratos()*volumen*coste_Total_nitratos;
        if (aguaTanque.getTotal_sulfatos() > 400) coste = coste + aguaTanque.getTotal_sulfatos()*volumen*coste_Total_sulfatos;
        return coste;
    }


    private class RepetirProcesosIndustrialesBehaviour extends TickerBehaviour{

        private ExtraerAguaBehaviour b;
        private ACLMessage request;

        private class ExtraerAguaBehaviour extends SimpleAchieveREInitiator{

            ExtraerAguaBehaviour(Agent a, ACLMessage msg) {
                super(a, msg);
            }

            protected ACLMessage prepareRequest(ACLMessage msg) {
                System.out.println(myAgent.getLocalName() + ": quiero extraer agua del río");
                return msg;
            }

            protected void handleInform(ACLMessage inform) {
                System.out.println(myAgent.getLocalName() + ": rio acepta extraccion de agua");
                try {
                    Action accion = (Action) getContentManager().extractContent(inform);
                    MasaDeAgua masa = (MasaDeAgua) accion.getAction();
                    masa.ensuciarAguas();
                    aguaTanque.mezclarAguas(masa);
                }
                catch(Exception ex) { ex.printStackTrace(); }
            }

            protected void handleRefuse(ACLMessage refuse) {
                System.out.println(myAgent.getLocalName()+ ": rio NO acepta extraccion de agua");
            }
        }

        RepetirProcesosIndustrialesBehaviour(Agent a, long period) {
            super(a,period);
        }

        public void onTick() {
            request = new ACLMessage(ACLMessage.REQUEST);
            request.setLanguage(codec.getName());
            request.setOntology(ontology.getName());
            request.addReceiver(rioAID);
            request.setSender(myAgent.getAID());
            request.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
            float volumenRestante = capacidad - aguaTanque.getVolumen();
            if (volumenRestante > 0) {
                ExtraerAgua extraccion = new ExtraerAgua();
                extraccion.setVolumen(volumenRestante);
                System.out.println(myAgent.getLocalName() + ": repite proceso coger agua");
                try {
                    Action accion = new Action(rioAID, extraccion);
                    getContentManager().fillContent(request, accion);
                }
                catch (Exception ex) { ex.printStackTrace(); }
                b = new ExtraerAguaBehaviour(myAgent, request);
                myAgent.addBehaviour(b);
            }
            else {
                System.out.println(myAgent.getLocalName() + ": tanque lleno");
            }
        }
    }

    private class RepetirVaciarTanque extends TickerBehaviour {

        private SolicitarVertidoBehaviour b;
        private ACLMessage mensaje;
        private SolicitarVertido solicitud;
        private VertirAgua vertido;
        private Action accion;
        private boolean hacerByPass;
        private Random random;

        private class SolicitarVertidoBehaviour extends SimpleAchieveREInitiator {

            SolicitarVertidoBehaviour(Agent a, ACLMessage msg) {
                super(a, msg);
            }

            protected void handleInform(ACLMessage inform) {
                System.out.println(getLocalName() + ": vertido a la EDAR realizado con éxito");
                presupuesto = presupuesto - calcularCosteDepuracion();
                aguaTanque = new MasaDeAgua();
            }

            protected void handleRefuse(ACLMessage refuse) {
                System.out.println(getLocalName() + ": vertido a la EDAR NO realizado");
            }
        }

        RepetirVaciarTanque(Agent a, long period) {
            super(a, period);
        }

        protected void onTick(){
            mensaje = new ACLMessage(ACLMessage.REQUEST);
            mensaje.setLanguage(codec.getName());
            mensaje.setOntology(ontology.getName());
            mensaje.setSender(getAID());
            random = new Random();
            hacerByPass = random.nextBoolean();
            if (aguaTanque.getVolumen() > 0) {
                MasaDeAgua masaEnviar = new MasaDeAgua(aguaTanque);
                if (estaLloviendo && hacerByPass) {
                    vertido = new VertirAgua();
                    vertido.setMasaDeAgua(masaEnviar);
                    accion = new Action(rioAID, vertido);
                    System.out.println(getLocalName() + ": hago un bypass al río");
                    mensaje.setPerformative(ACLMessage.INFORM);
                    mensaje.addReceiver(rioAID);
                    try {
                        getContentManager().fillContent(mensaje, accion);
                    }
                    catch (Exception ex) { ex.printStackTrace(); }
                    send(mensaje);
                    aguaTanque = new MasaDeAgua();
                }
                else if (calcularCosteDepuracion() < presupuesto){
                    System.out.println(getLocalName() + ": solicita a la EDAR vertir el agua del tanque");
                    mensaje.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
                    mensaje.addReceiver(edarAID);
                    solicitud = new SolicitarVertido();
                    solicitud.setMasaDeAgua(masaEnviar);
                    accion = new Action(edarAID, solicitud);
                    try {
                        getContentManager().fillContent(mensaje, accion);
                    }
                    catch (Exception ex) { ex.printStackTrace(); }
                    b = new SolicitarVertidoBehaviour(myAgent, mensaje);
                    myAgent.addBehaviour(b);
                }
            }
        }
    }

    private class ResponderContractNetBehaviour extends ContractNetResponder {

        ResponderContractNetBehaviour(Agent a, MessageTemplate mt) {
            super(a, mt);
        }

        protected ACLMessage handleCfp(ACLMessage cfp){
            ACLMessage reply = new ACLMessage(ACLMessage.REFUSE);
            reply.setSender(getAID());
            reply.addReceiver(edarAID);
            reply.setOntology(ontology.getName());
            reply.setLanguage(codec.getName());
            reply.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
            if (aguaTanque.getVolumen() > 0){
                System.out.println(myAgent.getLocalName() + ": acepta solicitud de vertido de EDAR");
                reply.setPerformative(ACLMessage.PROPOSE);
                Propuesta propuesta = new Propuesta();
                MasaDeAgua masaPropuesta = new MasaDeAgua(aguaTanque);
                propuesta.setMasaDeAgua(masaPropuesta);
                propuesta.setPresupuesto(presupuesto);
                Action accion = new Action(edarAID, propuesta);
                try {
                    getContentManager().fillContent(reply, accion);
                }
                catch(Exception ex) { ex.printStackTrace(); }
            }
            else {
                System.out.println(myAgent.getLocalName() + ": rechaza solicitud de vertido de EDAR");
            }
            return reply;
        }

        protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) {
            ACLMessage inform = accept.createReply();
            inform.setPerformative(ACLMessage.FAILURE);
            try {
                Action accion = (Action) getContentManager().extractContent(accept);
                ResultadoPropuesta resultado = (ResultadoPropuesta) accion.getAction();
                float volumenAceptado = resultado.getVolumen();
                float costeAsignado = resultado.getCoste();
                if (presupuesto > costeAsignado) {
                    inform.setPerformative(ACLMessage.INFORM);
                    MasaDeAgua masaAEnviar = new MasaDeAgua(aguaTanque);
                    masaAEnviar.setVolumen(volumenAceptado);
                    if (aguaTanque.getVolumen() == volumenAceptado) {
                        aguaTanque = new MasaDeAgua();
                    }
                    else {
                        aguaTanque.setVolumen(aguaTanque.getVolumen() - volumenAceptado);
                    }
                    presupuesto = presupuesto - costeAsignado;
                    Propuesta propuesta = new Propuesta();
                    propuesta.setMasaDeAgua(masaAEnviar);
                    accion = new Action(edarAID, propuesta);
                    getContentManager().fillContent(inform, propuesta);
                }
            }
            catch (Exception ex) { ex.printStackTrace(); }
            return inform;
        }
    }

    private class RepetirIncrementarPresupuestoBehaviour extends TickerBehaviour {

        RepetirIncrementarPresupuestoBehaviour(Agent a, long period) {
            super(a, period);
        }

        protected void onTick() {
            presupuesto = presupuesto + 5000;
            System.out.println(myAgent.getLocalName() + ": ha recibido un aumento de presupuesto");
        }
    }

    private class ComprobarSituacionLluvia extends CyclicBehaviour {

        ComprobarSituacionLluvia(Agent a) {
            super(a);
        }

        public void action() {
            MessageTemplate mt = MessageTemplate.and(
                    MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                    MessageTemplate.MatchSender(lluviaAID));
            ACLMessage inform = receive(mt);
            if (inform != null) {
                estaLloviendo = inform.getContent().equals("Se pone a llover");
            }
            else {
                block();
            }
        }
    }

    protected void setup() {
        Object [] args = getArguments();
        if (args != null) {
            String s;
            for (int i = 0; i < args.length; ++i) {
                s = args[i].toString();
                if (i == 0) {
                    capacidad = Integer.parseInt(s);
                }
                else if (i == 1) {
                    rioAID = new AID((String) args[i], AID.ISLOCALNAME);
                }
                else if (i == 2) {
                    edarAID = new AID((String) args[i], AID.ISLOCALNAME);
                }
                else {
                    lluviaAID = new AID((String) args[i], AID.ISLOCALNAME);
                }
            }
        }
        
        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(ontology);

        MessageTemplate mt = MessageTemplate.and(
            MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
            MessageTemplate.MatchPerformative(ACLMessage.CFP));
        ResponderContractNetBehaviour b = new ResponderContractNetBehaviour(this, mt);
        addBehaviour(b);
        ComprobarSituacionLluvia b2 = new ComprobarSituacionLluvia(this);
        addBehaviour(b2);
        RepetirProcesosIndustrialesBehaviour b3 = new RepetirProcesosIndustrialesBehaviour(this, 5000);
        addBehaviour(b3);
        RepetirVaciarTanque b4 = new RepetirVaciarTanque(this, 2000);
        addBehaviour(b4);
        RepetirIncrementarPresupuestoBehaviour b5 = new RepetirIncrementarPresupuestoBehaviour(this,10000);
        addBehaviour(b5);
    }

}
