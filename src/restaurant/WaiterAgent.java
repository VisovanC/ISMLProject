package restaurant;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.*;

public class WaiterAgent extends Agent {
    private List<String> availableTables;
    private Map<String, String> tableOrders; // table -> order
    private boolean isBusy = false;

    protected void setup() {
        // Initialize tables
        availableTables = new ArrayList<>();
        tableOrders = new HashMap<>();

        // Add some tables
        for (int i = 1; i <= 5; i++) {
            availableTables.add("Table" + i);
        }

        System.out.println("Waiter Agent " + getLocalName() + " is ready with " +
                availableTables.size() + " tables.");

        // Add behaviours
        addBehaviour(new CustomerServiceBehaviour());
        addBehaviour(new OrderProcessingBehaviour());
        addBehaviour(new TableManagementBehaviour());
    }

    private class CustomerServiceBehaviour extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.and(
                    MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                    MessageTemplate.MatchContent("REQUEST_TABLE")
            );

            ACLMessage msg = myAgent.receive(mt);

            if (msg != null) {
                ACLMessage reply = msg.createReply();

                if (!availableTables.isEmpty()) {
                    String table = availableTables.remove(0);
                    reply.setPerformative(ACLMessage.AGREE);
                    reply.setContent("TABLE_ASSIGNED:" + table);
                    System.out.println(myAgent.getLocalName() + ": Assigned " + table +
                            " to " + msg.getSender().getLocalName());
                } else {
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent("NO_TABLES_AVAILABLE");
                    System.out.println(myAgent.getLocalName() +
                            ": No tables available for " + msg.getSender().getLocalName());
                }

                send(reply);
            } else {
                block();
            }
        }
    }

    private class OrderProcessingBehaviour extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.and(
                    MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                    MessageTemplate.MatchContent("ORDER")
            );

            ACLMessage msg = myAgent.receive(mt);

            if (msg != null) {
                String customerName = msg.getSender().getLocalName();

                // Extract order details
                ACLMessage orderMsg = receive(MessageTemplate.MatchSender(msg.getSender()));
                if (orderMsg != null) {
                    String orderDetails = orderMsg.getContent();
                    System.out.println(myAgent.getLocalName() +
                            ": Received order from " + customerName + ": " + orderDetails);

                    // Forward order to chef
                    ACLMessage chefMsg = new ACLMessage(ACLMessage.REQUEST);
                    chefMsg.addReceiver(new AID("Chef", AID.ISLOCALNAME));
                    chefMsg.setContent("PREPARE_ORDER:" + customerName + ":" + orderDetails);
                    chefMsg.setConversationId("order-" + System.currentTimeMillis());
                    send(chefMsg);

                    // Store order info
                    tableOrders.put(customerName, orderDetails);

                    // Confirm to customer
                    ACLMessage confirm = msg.createReply();
                    confirm.setPerformative(ACLMessage.CONFIRM);
                    confirm.setContent("ORDER_RECEIVED");
                    send(confirm);
                }
            } else {
                block();
            }
        }
    }

    private class TableManagementBehaviour extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.and(
                    MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                    MessageTemplate.MatchContent("LEAVING")
            );

            ACLMessage msg = myAgent.receive(mt);

            if (msg != null) {
                String customerName = msg.getSender().getLocalName();

                // Find which table to free
                for (int i = 1; i <= 5; i++) {
                    String table = "Table" + i;
                    if (!availableTables.contains(table)) {
                        availableTables.add(table);
                        tableOrders.remove(customerName);
                        System.out.println(myAgent.getLocalName() + ": " + table +
                                " is now available (customer left)");
                        break;
                    }
                }

                // Send bill
                ACLMessage bill = msg.createReply();
                bill.setPerformative(ACLMessage.INFORM);
                bill.setContent("BILL:$" + (20 + new Random().nextInt(30)));
                send(bill);
            }

            // Also check for ready orders from chef
            MessageTemplate readyMt = MessageTemplate.and(
                    MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                    MessageTemplate.MatchSender(new AID("Chef", AID.ISLOCALNAME))
            );

            ACLMessage readyMsg = myAgent.receive(readyMt);
            if (readyMsg != null && readyMsg.getContent().startsWith("ORDER_READY:")) {
                String[] parts = readyMsg.getContent().split(":");
                String customerName = parts[1];

                // Deliver to customer
                ACLMessage delivery = new ACLMessage(ACLMessage.INFORM);
                delivery.addReceiver(new AID(customerName, AID.ISLOCALNAME));
                delivery.setContent("FOOD_SERVED");
                send(delivery);

                System.out.println(myAgent.getLocalName() +
                        ": Delivered food to " + customerName);
            }

            if (msg == null && readyMsg == null) {
                block();
            }
        }
    }

    protected void takeDown() {
        System.out.println("Waiter Agent " + getLocalName() + " terminating.");
    }
}