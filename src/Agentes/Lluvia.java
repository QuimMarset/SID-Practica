
package Agentes;

import Ontologia.EnviarLluvia;
import Ontologia.MasaDeAgua;
import Ontologia.OntologyMgr;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class Lluvia extends Agent{
    private Codec codec = new SLCodec();
    private Ontology ontology = OntologyMgr.getInstance();
    private List <AID> industrias = new ArrayList <> ();
    private AID edarAID;
    private AID rioAID;
    private MasaDeAgua aguaLluvia;
    
    public Lluvia() {
        super();
        aguaLluvia = new MasaDeAgua(1, 2.5f, 30, 25, 20, 0);
    }
    
    private class LloverBehaviour extends SimpleBehaviour {

        private ACLMessage msgIndustrias;
        private ACLMessage msgEDAR;
        private ACLMessage msgRio;
        private EnviarLluvia envioLluvia;
        private Random random;
        private Action accion;
        private float volumenLluvia;
        private MasaDeAgua aguaAEnviar;
        private int state;
        
        LloverBehaviour(Agent a) {
            super(a);
            state = -1;
            random = new Random();
        }

        private void enviarMensajes(boolean estaLloviendo) {
            msgIndustrias = new ACLMessage(ACLMessage.INFORM);
            msgIndustrias.setSender(getAID());
            for (AID industriaAID : industrias) {
                msgIndustrias.addReceiver(industriaAID);
            }

            msgEDAR = new ACLMessage(ACLMessage.INFORM);
            msgEDAR.setSender(getAID());
            msgEDAR.addReceiver(edarAID);
            msgEDAR.setOntology(ontology.getName());
            msgEDAR.setLanguage(codec.getName());

            msgRio = new ACLMessage(ACLMessage.INFORM);
            msgRio.setSender(getAID());
            msgRio.addReceiver(rioAID);
            msgRio.setOntology(ontology.getName());
            msgRio.setLanguage(codec.getName());

            if (estaLloviendo) {
                //Mensaje de la lluvia a todas las industrias
                msgIndustrias.setContent("Se pone a llover");
                send(msgIndustrias);
                // Mensaje de la lluvia a la EDAR
                volumenLluvia = random.nextInt(2000 - 1000 + 1) + 1000;
                aguaAEnviar = new MasaDeAgua(aguaLluvia);
                aguaAEnviar.setVolumen(volumenLluvia);
                envioLluvia = new EnviarLluvia();
                envioLluvia.setMasaDeAgua(aguaAEnviar);
                accion = new Action(edarAID, envioLluvia);
                try {
                    getContentManager().fillContent(msgEDAR, accion);
                }
                catch(Exception ex) { ex.printStackTrace(); }
                send(msgEDAR);
                // Mensaje de la lluvia al río
                volumenLluvia = random.nextInt(10000 - 5000 + 1) + 5000;
                aguaAEnviar = new MasaDeAgua(aguaLluvia);
                aguaAEnviar.setVolumen(volumenLluvia);
                envioLluvia = new EnviarLluvia();
                envioLluvia.setMasaDeAgua(aguaLluvia);
                accion  = new Action(rioAID, envioLluvia);
                try {
                    getContentManager().fillContent(msgRio, accion);
                }
                catch(Exception ex) { ex.printStackTrace(); }
                send(msgRio);
            }
            else {
                msgIndustrias.setContent("Deja de llover");
                send(msgIndustrias);
            }
        }

        public void action() {
            switch (state) {
                case -1:
                    state = 0;
                    block(30000);
                    break;
                case 0:
                    System.out.println("Se pone a llover");
                    enviarMensajes(true);
                    state = 1;
                    block(10000);
                    break;
                case 1:
                    System.out.println("Deja de llover");
                    enviarMensajes(false);
                    state = 0;
                    block(30000);
                    break;
            }
        }

        public boolean done() {
            return false;
        }
    }
    
    protected void setup() {
        Object [] args = getArguments();
        if (args != null) {
            String s;
            int num = args.length - 3;
            for (int i = 0; i < args.length; ++i) {
                s = args[i].toString();
                if (i < num + 1) {
                    industrias.add(new AID((String) args[i], AID.ISLOCALNAME));
                }
                else if (i ==  num + 1) {
                    rioAID = new AID((String) args[i], AID.ISLOCALNAME);
                }
                else if (i ==  num + 2) {
                    edarAID = new AID((String) args[i], AID.ISLOCALNAME);
                }
            }
        }
         // Register language and ontology
        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(ontology);
        
        LloverBehaviour llover = new LloverBehaviour(this);
        addBehaviour(llover);
    }

}
