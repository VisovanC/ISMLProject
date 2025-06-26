package restaurant;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.*;

public class ChefAgent extends Agent {
    private Queue<OrderInfo> orderQueue;
    private boolean isCooking = false;
    private Map<String, Integer> cookingTimes;

    private class OrderInfo {
        String customerName;
        String orderDetails;
        String conversationId;

        OrderInfo(String customer, String details, String convId) {
            this.customerName = customer;
            this.orderDetails = details;
            this.conversationId = convId;
        }
    }

    protected void setup() {
        orderQueue = new LinkedList<>();
        initializeCookingTimes();

        System.out.println("Chef Agent " + getLocalName() + " is ready to cook!");

        // Add behaviours
        addBehaviour(new OrderReceivingBehaviour());
        addBehaviour(new CookingBehaviour());
    }

    private void initializeCookingTimes() {
        cookingTimes = new HashMap<>();
        cookingTimes.put("Pizza", 8000);    // 8 seconds
        cookingTimes.put("Pasta", 6000);    // 6 seconds
        cookingTimes.put("Salad", 3000);    // 3 seconds
        cookingTimes.put("Burger", 5000);   // 5 seconds
        cookingTimes.put("Steak", 10000);   // 10 seconds
        cookingTimes.put("Soup", 4000);     // 4 seconds
    }

    private class OrderReceivingBehaviour extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.and(
                    MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                    MessageTemplate.MatchSender(new AID("Waiter", AID.ISLOCALNAME))
            );

            ACLMessage msg = myAgent.receive(mt);

            if (msg != null && msg.getContent().startsWith("PREPARE_ORDER:")) {
                String[] parts = msg.getContent().split(":", 3);
                String customerName = parts[1];
                String orderDetails = parts[2];

                OrderInfo order = new OrderInfo(customerName, orderDetails, msg.getConversationId());
                orderQueue.add(order);

                System.out.println(myAgent.getLocalName() +
                        ": Received order for " + customerName + ": " + orderDetails);
                System.out.println(myAgent.getLocalName() +
                        ": Orders in queue: " + orderQueue.size());

                // Send acknowledgment
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.AGREE);
                reply.setContent("ORDER_ACCEPTED");
                send(reply);
            } else {
                block();
            }
        }
    }

    private class CookingBehaviour extends CyclicBehaviour {
        public void action() {
            if (!isCooking && !orderQueue.isEmpty()) {
                OrderInfo order = orderQueue.poll();
                isCooking = true;

                // Calculate total cooking time
                int totalTime = calculateCookingTime(order.orderDetails);

                System.out.println(myAgent.getLocalName() +
                        ": Starting to cook " + order.orderDetails +
                        " for " + order.customerName +
                        " (estimated time: " + (totalTime/1000) + " seconds)");

                // Simulate cooking
                myAgent.addBehaviour(new WakerBehaviour(myAgent, totalTime) {
                    protected void onWake() {
                        System.out.println(myAgent.getLocalName() +
                                ": Finished cooking for " + order.customerName);

                        // Notify waiter that order is ready
                        ACLMessage ready = new ACLMessage(ACLMessage.INFORM);
                        ready.addReceiver(new AID("Waiter", AID.ISLOCALNAME));
                        ready.setContent("ORDER_READY:" + order.customerName);
                        ready.setConversationId(order.conversationId);
                        send(ready);

                        isCooking = false;
                    }
                });
            }

            block(1000); // Check every second
        }
    }

    private int calculateCookingTime(String orderDetails) {
        int totalTime = 0;
        String[] items = orderDetails.split(", ");

        for (String item : items) {
            Integer time = cookingTimes.get(item.trim());
            if (time != null) {
                totalTime = Math.max(totalTime, time); // Cook in parallel
            }
        }

        // Add some prep time
        totalTime += 2000;

        return totalTime;
    }

    protected void takeDown() {
        System.out.println("Chef Agent " + getLocalName() + " is closing the kitchen.");
    }
}