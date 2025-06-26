package restaurant;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.*;

public class CustomerAgent extends Agent {
    private String assignedTable = null;
    private String[] menuItems = {"Pizza", "Pasta", "Salad", "Burger", "Steak", "Soup"};
    private boolean hasOrdered = false;
    private boolean hasEaten = false;
    private Random random = new Random();

    protected void setup() {
        System.out.println("Customer Agent " + getLocalName() + " entered the restaurant.");

        // Add behaviours
        addBehaviour(new OneShotBehaviour() {
            public void action() {
                // Request a table from waiter
                ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                msg.addReceiver(new AID("Waiter", AID.ISLOCALNAME));
                msg.setContent("REQUEST_TABLE");
                send(msg);
                System.out.println(myAgent.getLocalName() + ": Requesting a table...");
            }
        });

        addBehaviour(new TableAssignmentBehaviour());
        addBehaviour(new OrderingBehaviour());
        addBehaviour(new EatingBehaviour());
    }

    private class TableAssignmentBehaviour extends CyclicBehaviour {
        public void action() {
            if (assignedTable == null) {
                MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.AGREE);
                ACLMessage msg = myAgent.receive(mt);

                if (msg != null && msg.getContent().startsWith("TABLE_ASSIGNED:")) {
                    assignedTable = msg.getContent().substring(15);
                    System.out.println(myAgent.getLocalName() + ": Got " + assignedTable);

                    // Wait a bit before ordering
                    myAgent.addBehaviour(new WakerBehaviour(myAgent, 2000) {
                        protected void onWake() {
                            // Trigger ordering
                            hasOrdered = false;
                        }
                    });
                }

                // Check for refusal
                MessageTemplate refuseMt = MessageTemplate.MatchPerformative(ACLMessage.REFUSE);
                ACLMessage refuseMsg = myAgent.receive(refuseMt);
                if (refuseMsg != null) {
                    System.out.println(myAgent.getLocalName() +
                            ": No tables available. Leaving restaurant.");
                    doDelete();
                }
            }

            block();
        }
    }

    private class OrderingBehaviour extends CyclicBehaviour {
        public void action() {
            if (assignedTable != null && !hasOrdered) {
                // Decide what to order
                int numItems = random.nextInt(3) + 1; // Order 1-3 items
                StringBuilder order = new StringBuilder();

                for (int i = 0; i < numItems; i++) {
                    if (i > 0) order.append(", ");
                    order.append(menuItems[random.nextInt(menuItems.length)]);
                }

                // Send order to waiter
                ACLMessage orderMsg = new ACLMessage(ACLMessage.INFORM);
                orderMsg.addReceiver(new AID("Waiter", AID.ISLOCALNAME));
                orderMsg.setContent("ORDER");
                send(orderMsg);

                // Send order details
                ACLMessage details = new ACLMessage(ACLMessage.INFORM);
                details.addReceiver(new AID("Waiter", AID.ISLOCALNAME));
                details.setContent(order.toString());
                send(details);

                System.out.println(myAgent.getLocalName() + ": Ordering " + order.toString());
                hasOrdered = true;
            }

            // Wait for order confirmation
            if (hasOrdered && !hasEaten) {
                MessageTemplate mt = MessageTemplate.and(
                        MessageTemplate.MatchPerformative(ACLMessage.CONFIRM),
                        MessageTemplate.MatchContent("ORDER_RECEIVED")
                );
                ACLMessage msg = myAgent.receive(mt);

                if (msg != null) {
                    System.out.println(myAgent.getLocalName() + ": Order confirmed by waiter");
                }
            }

            block();
        }
    }

    private class EatingBehaviour extends CyclicBehaviour {
        public void action() {
            if (hasOrdered && !hasEaten) {
                MessageTemplate mt = MessageTemplate.and(
                        MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                        MessageTemplate.MatchContent("FOOD_SERVED")
                );
                ACLMessage msg = myAgent.receive(mt);

                if (msg != null) {
                    System.out.println(myAgent.getLocalName() + ": Food received! Eating...");
                    hasEaten = true;

                    // Simulate eating time
                    myAgent.addBehaviour(new WakerBehaviour(myAgent, 5000 + random.nextInt(5000)) {
                        protected void onWake() {
                            // Leave restaurant
                            ACLMessage leaving = new ACLMessage(ACLMessage.INFORM);
                            leaving.addReceiver(new AID("Waiter", AID.ISLOCALNAME));
                            leaving.setContent("LEAVING");
                            send(leaving);
                            System.out.println(myAgent.getLocalName() + ": Finished eating, asking for bill");
                        }
                    });
                }
            }

            // Check for bill
            MessageTemplate billMt = MessageTemplate.and(
                    MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                    MessageTemplate.MatchSender(new AID("Waiter", AID.ISLOCALNAME))
            );
            ACLMessage billMsg = myAgent.receive(billMt);

            if (billMsg != null && billMsg.getContent().startsWith("BILL:")) {
                System.out.println(myAgent.getLocalName() + ": Received " +
                        billMsg.getContent() + ". Paying and leaving. Thank you!");
                doDelete();
            }

            block();
        }
    }

    protected void takeDown() {
        System.out.println("Customer Agent " + getLocalName() + " left the restaurant.");
    }
}